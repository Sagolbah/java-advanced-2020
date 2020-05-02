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
    public void start(int port, int threads) {
        try {
            socket = new DatagramSocket(port);
            listeners = Executors.newFixedThreadPool(threads);
            final int packetSize = socket.getSendBufferSize();
            for (int i = 0; i < threads; i++) {
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
}
