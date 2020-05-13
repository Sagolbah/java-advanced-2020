package ru.ifmo.rain.boger.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPClient implements HelloClient {
    @Override
    public void run(String host, int port, String prefix, int threadCount, int requestCount) {
        SocketAddress address;
        try {
            address = new InetSocketAddress(InetAddress.getByName(host), port);
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + host);
            return;
        }

        final ExecutorService sendersThreadPool = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int finalI = i;
            sendersThreadPool.submit(() -> send(prefix, finalI, requestCount, address, latch));
        }

        try {
            latch.await();
        } catch (InterruptedException ignored) {
            // No operations.
        } finally {
            sendersThreadPool.shutdownNow();
        }
    }

    private void send(String prefix, int threadId, int requestsCount, SocketAddress address, CountDownLatch latch) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(500);
            DatagramPacket requestPacket = new DatagramPacket(new byte[0], 0, address);
            DatagramPacket responsePacket = new DatagramPacket(new byte[socket.getReceiveBufferSize()], socket.getReceiveBufferSize());
            for (int requestId = 0; requestId < requestsCount; requestId++) {
                String request = getRequestString(prefix, threadId, requestId);
                requestPacket.setData(request.getBytes(StandardCharsets.UTF_8));
                while (!socket.isClosed()) {
                    try {
                        socket.send(requestPacket);
                        System.out.println("Request sent: " + request);
                        socket.receive(responsePacket);
                        String response = getString(responsePacket);
                        if (response.matches("[\\D]*" + threadId + "[\\D]*" + requestId + "[\\D]*")) {
                            System.out.println("Response received: " + response);
                            break;
                        }
                    } catch (IOException e) {
                        System.err.println("Processing failed: " + e.getMessage());
                    }
                }
            }
        } catch (SocketException ignored) {
            // No operations.
        } finally {
            latch.countDown();
        }
    }

    private String getString(final DatagramPacket packet) {
        return new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    private String getRequestString(final String prefix, final int threadId, final int requestId) {
        return prefix + threadId + '_' + requestId;
    }

    public static void main(String[] args) {
        if (args == null || args.length != 5) {
            System.out.println("Usage: java HelloUDPClient <host> <port> <query prefix> <threads> <requests in thread>");
            return;
        }
        for (final String s : args) {
            if (s == null) {
                System.out.println("Arguments must be non-null");
                return;
            }
        }
        try {
            new HelloUDPClient().run(args[0], Integer.parseInt(args[1]), args[2],
                    Integer.parseInt(args[3]), Integer.parseInt(args[4]));
        } catch (NumberFormatException e) {
            System.out.println("<port>, <threads>, <requests in thread> arguments must be numbers");
        }
    }
}
