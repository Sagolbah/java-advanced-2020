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
    private final int perHost;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
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

    private void extractAndRunBFS(Document document, Set<String> successes, Map<String, IOException> fails, Set<String> visited, int depth, Phaser phaser) {
        try {
            List<String> links = document.extractLinks();
            for (final String link : links) {
                if (!visited.contains(link)) {
                    visited.add(link);
                    downloadWithBFS(link, successes, fails, visited, depth, phaser);
                }
            }
        } catch (IOException e) {
            // No operations (Result says should only store downloading, not extraction fails?).
        } finally {
            phaser.arrive();
        }
    }

    private void downloadWithBFS(String url, Set<String> successes, Map<String, IOException> fails, Set<String> visited, int depth, Phaser phaser) {
        phaser.register();
        downloadersThreadPool.submit(() -> {
            try {
                Document result = downloader.download(url);
                successes.add(url);
                if (depth > 1) {
                    // Should wait not only for downloads, but for extractions too.
                    phaser.register();
                    extractorsThreadPool.submit(() -> extractAndRunBFS(result, successes, fails, visited, depth - 1, phaser));
                }
            } catch (IOException e) {
                fails.put(url, e);
            } finally {
                phaser.arrive();
            }
        });
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
                System.out.println("<downloaders>, <extractors> ans <perHost> must be numbers");
            }
        }
        try (WebCrawler crawler = new WebCrawler(new CachingDownloader(), numberArguments[1], numberArguments[2], numberArguments[3])) {
            crawler.download(args[0], numberArguments[0]);
        } catch (IOException e) {
            System.out.println("Can't initialize downloader");
        }
    }

}
