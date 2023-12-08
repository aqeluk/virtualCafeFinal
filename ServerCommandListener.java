import java.io.IOException;
import java.util.Scanner;

public class ServerCommandListener extends Thread {
    private SocketServer server;

    public ServerCommandListener(SocketServer server) {
        this.server = server;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String input = scanner.nextLine();
            if ("exit".equalsIgnoreCase(input)) {
                try {
                    server.shutdown();
                } catch (IOException e) {
                    System.out.println("Error shutting down server: " + e.getMessage());
                }
                scanner.close();
                break;
            }
        }
    }
}