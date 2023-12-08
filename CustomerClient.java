import java.io.IOException;

import ClientHelpers.NetworkManager;
import ClientHelpers.UIManager;

public class CustomerClient {
    private static volatile boolean nameProvided = false;
    private static final Object lock = new Object();
    private static volatile boolean socketClosed = false;
    private UIManager gui;
    private NetworkManager networkManager;

    public CustomerClient() {
        this.gui = new UIManager();
        this.networkManager = new NetworkManager();
    }

    private static void sendLeavingMessage(NetworkManager networkManager) {
        try {
            String leavingMessage = "Client has left";
            networkManager.send(leavingMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CustomerClient client = new CustomerClient();
        try {
            client.networkManager.connect();
            client.gui.updateServerResponse("Connected to the server.");
        } catch (IOException e) {
            client.gui.updateServerResponse("Failed to connect to the server.");
            return;  // If we can't connect, end the program.
        }

        // Dedicated thread to listen for server messages
        Thread listeningThread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String serverMessage = client.networkManager.receive();
                    if (serverMessage != null) {
                        client.gui.updateServerResponse(serverMessage);
                        if (serverMessage.startsWith("Welcome to the restaurant,")) {
                            nameProvided = true;
                            client.gui.setShowHelp(true); // Display help once name is acknowledged
                        }
                        synchronized (lock) {
                            lock.notify();
                        }
                    }
                }
            } catch (IOException e) {
                if (!(e instanceof java.nio.channels.ClosedByInterruptException)) {
                    e.printStackTrace();
                }
            }
        });
        listeningThread.start();

        // Registering a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Gracefully shutting down...");
            if (!socketClosed) {
                sendLeavingMessage(client.networkManager);
            }
            listeningThread.interrupt();
            client.networkManager.close();
        }));

        // Name provision loop
        while (!nameProvided) {
            client.gui.render();
            String name = client.gui.getUserInput();
            if (UIManager.isValidName(name)) {
                try {
                    client.networkManager.send(name);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                client.gui.setClientRequest("");
                synchronized (lock) {
                    try {
                        lock.wait(); // Wait for a server response after sending a name
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                client.gui.updateServerResponse("Invalid name. Please enter a valid name.");
            }
        }

        // Command input loop
        while (true) {
            client.gui.render();
            String clientCommand = client.gui.getUserInput();
            try {
                client.networkManager.send(clientCommand);
            } catch (IOException e) {
                e.printStackTrace();
            }
            client.gui.setClientRequest("");
            if (clientCommand.equalsIgnoreCase("leave")) {
                listeningThread.interrupt();
                client.networkManager.close();
                socketClosed = true;
                break;
            }
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}