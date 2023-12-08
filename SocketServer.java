import ServerHelpers.*;
import java.io.IOException;
import java.nio.channels.SocketChannel;

public class SocketServer implements OrderReadyCallback {

    private NetworkManager networkManager;
    private RequestHandler requestHandler;
    private ServerCommandListener commandListener;
    private Cafe cafe;

    public SocketServer() throws IOException {
        cafe = new Cafe();
        networkManager = new NetworkManager();
        requestHandler = new RequestHandler(cafe);
        commandListener = new ServerCommandListener(this);
        cafe.setOrderReadyCallback(this);
    }

    @Override
    public void onOrderReady(String customerName, Order order) {
        SocketChannel channel = networkManager.getChannelForName(customerName);
        if (channel != null) {
            try {
                networkManager.send(channel, "Your order is ready for pickup! " + order);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void startServer() throws IOException {
        System.out.println("Server started on port " + NetworkManager.getPort());
        commandListener.start();

        networkManager.startListening(new NetworkManager.ClientHandler() {
            @Override
            public void onNewClient(SocketChannel clientChannel) throws IOException {
                networkManager.send(clientChannel, "Please enter your name");
                System.out.println("A new client has connected, awaiting name...");
            }

            @Override
            public void onClientMessage(SocketChannel clientChannel, String message) throws IOException {
                String clientName = networkManager.getNameForClient(clientChannel);
                if (clientName != null && !clientName.isEmpty()) {
                    System.out.println(clientName + " sent command: " + message);
                    handleRequest(message, clientChannel);
                } else {
                    networkManager.associateNameWithClient(clientChannel, message);
                    networkManager.send(clientChannel, "Welcome to the restaurant, " + message);
                }
            }

            @Override
            public void onClientDisconnected(SocketChannel clientChannel) {
                String clientName = networkManager.getNameForClient(clientChannel);
                if (clientName != null) {
                    System.out.println(clientName + " has disconnected.");
                    networkManager.removeClient(clientChannel);
                } else {
                    System.out.println("An unknown client has disconnected.");
                }
            }
        });
    }

    private void handleRequest(String request, SocketChannel channel) throws IOException {
        String clientName = networkManager.getNameForClient(channel);
        if (clientName == null) {
            clientName = "UNKNOWN";
        }
        String response = requestHandler.handle(request, clientName);
        networkManager.send(channel, response);
    }

    public void shutdown() throws IOException {
        System.out.println("Shutting down server...");
        networkManager.shutdown(); // Signal the server to stop

        // Wait a short moment to ensure the main loop has stopped
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
            // Handle the interruption if necessary
        }

        networkManager.closeResources(); // Close resources once we're sure the loop has stopped

        System.exit(0); // Shutdown the whole JVM
    }

    public static void main(String[] args) throws IOException {
        new SocketServer().startServer();
    }
}
