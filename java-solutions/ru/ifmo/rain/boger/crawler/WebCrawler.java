package ru.ifmo.rain.boger.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements Crawler {
    private final Downloader downloader;
    private final ExecutorService downloadersThreadPool;
    private final ExecutorService extractorsThreadPool;
    private final ConcurrentMap<String, HostMonitor> hostsData;
    private final int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.hostsData = new ConcurrentHashMap<>();
        this.downloader = downloader;
        downloadersThreadPool = Executors.newFixedThreadPool(downloaders);
        extractorsThreadPool = Executors.newFixedThreadPool(extractors);
        this.perHost = perHost;
    }

    @Override
    public Result download(String url, int depth) {
        // We don't know how many pages there will be, so we should use Phaser instead of CyclicBarrier/CountdownLatch
        Phaser phaser = new Phaser(1);
        Set<String> visited = ConcurrentHashMap.newKeySet();
        Set<String> successes = ConcurrentHashMap.newKeySet();
        Map<String, IOException> fails = new ConcurrentHashMap<>();
        visited.add(url);
        downloadWithBFS(url, successes, fails, visited, depth, phaser);
        phaser.arriveAndAwaitAdvance();
        List<String> successList = new ArrayList<>(successes);
        return new Result(successList, fails);
    }

    @Override
    public void close() {
        downloadersThreadPool.shutdownNow();
        extractorsThreadPool.shutdownNow();
    }

    // Utility functions

    private void downloadWithBFS(String url, Set<String> successes, Map<String, IOException> fails, Set<String> visited, int depth, Phaser phaser) {
        try {
            String host = URLUtils.getHost(url);
            HostMonitor monitor = hostsData.computeIfAbsent(host, h -> new HostMonitor(perHost, downloadersThreadPool));
            phaser.register();
            monitor.addTask(() -> {
                try {
                    Document result = downloader.download(url);
                    successes.add(url);
                    if (depth > 1) {
                        phaser.register();
                        extractorsThreadPool.submit(() -> {
                            try {
                                result.extractLinks().stream().filter(visited::add).forEach(
                                        link -> downloadWithBFS(link, successes, fails, visited, depth - 1, phaser));
                            } catch (IOException ignored) {
                                // No operations.
                            } finally {
                                phaser.arrive();
                            }
                        });
                    }
                } catch (IOException e) {
                    fails.put(url, e);
                } finally {
                    phaser.arrive();
                    monitor.finishTask();
                }
            });
        } catch (MalformedURLException e) {
            fails.put(url, e);
        }
    }

    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.out.println("Usage: WebCrawler <url> <depth> <downloaders> <extractors> <perHost>");
            return;
        }
        for (String s : args) {
            if (s == null) {
                System.out.println("Arguments must be non-null");
                return;
            }
        }
        int[] numberArguments = new int[4];
        for (int i = 1; i < 5; i++) {
            try {
                numberArguments[i - 1] = Integer.parseInt(args[i]);
            } catch (NumberFormatException e) {
                System.out.println("<depth>, <downloaders>, <extractors> and <perHost> must be numbers");
            }
        }
        try (WebCrawler crawler = new WebCrawler(new CachingDownloader(), numberArguments[1], numberArguments[2], numberArguments[3])) {
            crawler.download(args[0], numberArguments[0]);
        } catch (IOException e) {
            System.out.println("Can't initialize downloader");
        }
    }


}
