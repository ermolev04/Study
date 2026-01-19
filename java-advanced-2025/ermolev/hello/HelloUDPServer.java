package info.kgeorgiy.ja.ermolev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static info.kgeorgiy.ja.ermolev.hello.Utils.*;
import static java.lang.Integer.parseInt;
import static java.util.Objects.isNull;

public class HelloUDPServer implements HelloServer {

    private ExecutorService receivers, senders;
    private DatagramSocket socket;

    @Override
    public void start(int port, int threads) {
        try {
            int recSize = 1, sendSize = 1;
            socket = new DatagramSocket(port);
            if (threads > 1) {
                recSize = (threads + 1) / 2;
                sendSize = threads / 2;
            }
            receivers = Executors.newFixedThreadPool(recSize);
            senders = Executors.newFixedThreadPool(sendSize);
            for (int i = 0; i < recSize; i++) {
                receivers.submit(this::receiver);
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private void receiver() {
        while (!Thread.interrupted()) {
            senders.submit(() -> {
                try {
                    DatagramPacket returnedPacket = recive(socket);
                    socket.receive(returnedPacket);
                    sender(returnedPacket);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void sender(DatagramPacket request) {
        try {
            request.setData(createBuf("Hello, ", new String(request.getData(), request.getOffset(), request.getLength(), StandardCharsets.UTF_8)));
            socket.send(request);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(final String[] args) {
        if (isNull(args) || args.length != 2) {
            System.err.println("We expect 2 arguments: port and threads");
            return;
        }
        try {
            int port = parseInt(args[0]);
            int threads = parseInt(args[1]);
            HelloServer server = new HelloUDPServer();
            server.start(port, threads);
            server.close();
        } catch (final NumberFormatException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public void close() {
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (receivers != null) {
            receivers.shutdownNow();
        }
        if (senders != null) {
            senders.shutdownNow();
        }
    }
}
