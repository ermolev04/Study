package info.kgeorgiy.ja.ermolev.hello;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Utils {
    static DatagramPacket recive(DatagramSocket socket) throws SocketException {
        try {
            byte[] buf = new byte[socket.getReceiveBufferSize()];
            DatagramPacket request = new DatagramPacket(buf, buf.length);
//            try {
//                socket.receive(request);
                return request;
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }

    }

    static InetAddress getAddress(String host) throws UnknownHostException {
        return InetAddress.getByName(host);
    }

    static byte[] createBuf(String prefix, String suffix) {
        String message = prefix + suffix;
        return message.getBytes(StandardCharsets.UTF_8);
    }
}
