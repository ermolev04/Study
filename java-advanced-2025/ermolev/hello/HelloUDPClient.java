package info.kgeorgiy.ja.ermolev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;
import info.kgeorgiy.java.advanced.hello.NewHelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.*;

import static info.kgeorgiy.ja.ermolev.hello.Utils.*;
import static java.lang.Integer.parseInt;
import static java.lang.Integer.valueOf;
import static java.util.Objects.isNull;

// :NOTE: создать Util класс общими методами для Client Server, создания пакетов и вноса пакетов
public class HelloUDPClient implements NewHelloClient {
    @Override
    public void newRun(List<Request> requests, int threads) {
        ExecutorService reqQue = Executors.newFixedThreadPool(threads);
        Phaser phaser = new Phaser(1);
        for(Request request : requests ) {
            for(int thread = 1; thread <= threads; thread++) {
                int finalThread = thread;
                phaser.register();
                reqQue.submit(() -> {
                    try {
                        InetAddress address = InetAddress.getByName(request.host());
                        String message = request.template().replace("$", String.valueOf(finalThread));
                        try (DatagramSocket socket = new DatagramSocket()) {
                            socket.setSoTimeout(200);
                            byte[] buf = message.getBytes(StandardCharsets.UTF_8);
                            DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, address, request.port());
                            while (!Thread.interrupted()) {
                                try {
                                    socket.send(sendPacket);
                                    DatagramPacket returnedPacket = recive(socket);
                                    socket.receive(returnedPacket);
                                    String returnedData = new String(returnedPacket.getData(), returnedPacket.getOffset(), returnedPacket.getLength(), StandardCharsets.UTF_8);
                                    if (returnedData.endsWith(message)) {
                                        break;
                                    }
                                } catch (IOException e) {
                                    System.out.println(e.getMessage());
                                }
                            }
                        }
                    } catch (IOException e) {
                    } finally {
                        phaser.arriveAndDeregister();
                    }
                });
            }
            phaser.arriveAndAwaitAdvance();
        }

        phaser.arriveAndDeregister();
        reqQue.shutdown();
        try {
            reqQue.awaitTermination(threads * requests.size(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void run(String host, int port, String prefix, int requests, int threads) {
        InetAddress address;
        try {
            address = getAddress(host);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
        ExecutorService reqQue = Executors.newFixedThreadPool(threads);
        for (int thread = 1; thread <= threads; thread++) {
            int fixThread = thread;
            reqQue.submit(() -> requester(fixThread, port, prefix, requests, address));
        }
        reqQue.shutdown();
        try {
            reqQue.awaitTermination(threads * requests, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void requester(int thread, int port, String prefix, int requests, InetAddress address){
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(200);
            for (int request = 1; request <= requests; request++) {
                byte[] buf = createBuf(prefix, "" + request + '_' + thread);
                DatagramPacket sendPacket = new DatagramPacket(buf, buf.length, address, port);
                while (!Thread.interrupted()) {
                    try {
                        socket.send(sendPacket);
                        byte[] supBuf = new byte[socket.getReceiveBufferSize()];
                        DatagramPacket returnedPacket = new DatagramPacket(supBuf, supBuf.length);
                        socket.receive(returnedPacket);
                        String returnedData = new String(returnedPacket.getData(), returnedPacket.getOffset(), returnedPacket.getLength(), StandardCharsets.UTF_8);
                        if (returnedData.endsWith(prefix + request + '_' + thread)) {
                            break;
                        }
                    } catch (IOException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        if (isNull(args) || args.length != 5) {
            System.err.println("We expect 5 arguments: host, port, prefix, threads and request");
            return;
        }
        try {
            HelloClient client = new HelloUDPClient();
            client.run(args[0], parseInt(args[1]), args[2], parseInt(args[3]), parseInt(args[4]));
        } catch (final NumberFormatException e) {
            System.err.println(e.getMessage());
        }
    }
}