package ru.ifmo.rain.boger.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService listeners;

    @Override
    public void start(int port, int threadsCount) {
        try {
            socket = new DatagramSocket(port);
            listeners = Executors.newFixedThreadPool(threadsCount);
            final int packetSize = socket.getSendBufferSize();
            for (int i = 0; i < threadsCount; i++) {
                listeners.submit(() -> listen(packetSize));
            }
        } catch (SocketException ignored) {
            // No operations.
        }
    }

    private void listen(final int packetSize) {
        while (!socket.isClosed()) {
            byte[] buffer = new byte[packetSize];
            DatagramPacket packet = new DatagramPacket(buffer, packetSize);
            try {
                socket.receive(packet);
                packet.setData(getMessage(packet).getBytes(StandardCharsets.UTF_8));
                socket.send(packet);
            } catch (IOException ignored) {
                // No operations
            }
        }
    }

    private String getMessage(final DatagramPacket packet) {
        return "Hello, " + new String(packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
    }

    @Override
    public void close() {
        listeners.shutdownNow();
        socket.close();
    }

    public static void main(String[] args) {
        if (args == null || args.length != 2) {
            System.out.println("Usage: java HelloUDPServer <port> <threads>");
            return;
        }
        if (args[0] == null || args[1] == null) {
            System.out.println("Arguments must be non-null");
            return;
        }
        try (HelloUDPServer server = new HelloUDPServer()) {
            server.start(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
        } catch (NumberFormatException e) {
            System.out.println("<port>, <threads> arguments must be numbers");
        }
    }
}
