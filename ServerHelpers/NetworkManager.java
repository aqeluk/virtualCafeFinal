package ServerHelpers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class NetworkManager {

    private static final int PORT = 9876;
    private static final int BUFFER_SIZE = 4096;
    private volatile boolean running = true;

    private Selector selector;
    private ByteBuffer readBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private ByteBuffer writeBuffer = ByteBuffer.allocate(BUFFER_SIZE);
    private Map<SocketChannel, String> clientNames = new HashMap<>();

    public NetworkManager() throws IOException {
        selector = Selector.open();
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.socket().bind(new InetSocketAddress(PORT));
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
    }

    public interface ClientHandler {
        void onNewClient(SocketChannel clientChannel) throws IOException;

        void onClientMessage(SocketChannel clientChannel, String message) throws IOException;

        void onClientDisconnected(SocketChannel clientChannel);
    }

    public static int getPort() {
        return PORT;
    }

    public void startListening(ClientHandler handler) throws IOException {
        while (running) {
            selector.select();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();

            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                keys.remove();

                if (!key.isValid()) {
                    continue;
                }

                if (key.isAcceptable()) {
                    SocketChannel clientChannel = accept(key);
                    handler.onNewClient(clientChannel);
                } else if (key.isReadable()) {
                    SocketChannel clientChannel = (SocketChannel) key.channel();
                    String message = read(clientChannel);
                    if (message == null) {
                        handler.onClientDisconnected(clientChannel);
                    } else {
                        handler.onClientMessage(clientChannel, message);
                    }
                }
            }
        }
    }

    private SocketChannel accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverSocket.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
        return clientChannel;
    }

    private String read(SocketChannel clientChannel) throws IOException {
        readBuffer.clear();
        int numRead;
        try {
            numRead = clientChannel.read(readBuffer);
        } catch (IOException e) {
            clientChannel.close();
            return null;
        }

        if (numRead == -1) {
            clientChannel.close();
            return null;
        }

        return new String(readBuffer.array(), 0, numRead).trim();
    }

    public void associateNameWithClient(SocketChannel clientChannel, String name) {
        clientNames.put(clientChannel, name);
        System.out.println(name + " has entered the cafe.");
    }

    public String getNameForClient(SocketChannel clientChannel) {
        return clientNames.get(clientChannel);
    }

    public void removeClient(SocketChannel clientChannel) {
        clientNames.remove(clientChannel);
    }
    
    public SocketChannel getChannelForName(String name) {
        for (SocketChannel channel : clientNames.keySet()) {
            if (clientNames.get(channel).equals(name)) {
                return channel;
            }
        }
        return null;
    }

    public void send(SocketChannel clientChannel, String message) throws IOException {
        writeBuffer.clear();
        writeBuffer.put(message.getBytes());
        writeBuffer.flip();
        while (writeBuffer.hasRemaining()) {
            clientChannel.write(writeBuffer);
        }
    }

    public void stopClient(SocketChannel clientChannel) throws IOException {
        String clientName = getNameForClient(clientChannel);
        if (clientName != null) {
            System.out.println(clientName + " has left the cafe.");
        }
        clientChannel.close();
        clientNames.remove(clientChannel);
    }

    public void shutdown() throws IOException {
        running = false;
        selector.wakeup(); // Interrupt the selector to check the running flag
    }

    public void closeResources() throws IOException {
        for (SocketChannel channel : clientNames.keySet()) {
            channel.close();
        }
        selector.close();
    }
}
