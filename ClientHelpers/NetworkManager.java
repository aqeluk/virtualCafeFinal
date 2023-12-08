package ClientHelpers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class NetworkManager {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 9876;
    private SocketChannel socketChannel;

    public void connect() throws IOException {
        this.socketChannel = SocketChannel.open();
        this.socketChannel.connect(new InetSocketAddress(SERVER_ADDRESS, SERVER_PORT));
    }

    public void send(String message) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(message.getBytes().length);
        buffer.clear();
        buffer.put(message.getBytes());
        buffer.flip();
        while (buffer.hasRemaining()) {
            socketChannel.write(buffer);
        }
    }

    public String receive() throws IOException {
        ByteBuffer readBuffer = ByteBuffer.allocate(1024);
        readBuffer.clear();
        int bytesRead = socketChannel.read(readBuffer);
        if (bytesRead > 0) {
            return new String(readBuffer.array(), 0, bytesRead).trim();
        }
        return null;
    }

    public void close() {
        try {
            if (socketChannel != null && socketChannel.isOpen()) {
                socketChannel.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
