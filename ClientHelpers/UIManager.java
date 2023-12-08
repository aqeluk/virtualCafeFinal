package ClientHelpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.regex.Pattern;

public class UIManager {

    public String getClientRequest() {
        return this.clientRequest;
    }

    public void setClientRequest(String clientRequest) {
        this.clientRequest = clientRequest;
    }

    private static int getTerminalHeight() {
        int height = 20; // default value
        try {
            Process process = new ProcessBuilder("bash", "-c", "tput lines").start();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line = reader.readLine();
                if (line != null) {
                    height = Integer.parseInt(line.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return height;
    }

    private static final int HEIGHT = getTerminalHeight() - 7; // 7 lines for the help text
    private static final String TITLE = AnsiStyles.bold("VIRTUAL CAFE");
    private static final String HELP = "Welcome!\n" +
            "-------------------------------\n" +
            "1. Place an order: 'order...'\n" +
            "   (Example: 'order 1 tea and 3 coffees')\n" +
            "2. Enquire about your pending orders with 'status'.\n" +
            "3. Leave the cafe with 'leave'.\n" +
            "-------------------------------\n" +
            "Please enter your command:";
    private String serverResponse = "";
    private String clientRequest = "";
    private boolean showHelp = false;
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z ]+$");

    public void setShowHelp(boolean showHelp) {
        this.showHelp = showHelp;
    }

    public synchronized void render() {
        clearConsole();
        System.out.println(TITLE);
        System.out.println("\n");
        if (showHelp) {
            System.out.println(HELP);
            for (int i = 0; i < HEIGHT - 6; i++) {
                System.out.println();
            }
        } else {
            for (int i = 0; i < HEIGHT + 2; i++) {
                System.out.println();
            }
        }
        System.out.println("Server says: " + serverResponse);
        System.out.print("> " + clientRequest);
    }

    public void updateServerResponse(String response) {
        serverResponse = response;
        render(); // re-render the screen whenever we get a new server response
    }

    public String getUserInput() {
        Scanner scanner = new Scanner(System.in);
        clientRequest = scanner.nextLine();
        return clientRequest;
    }

    public static void clearConsole() {
        for (int i = 0; i < HEIGHT; i++) {
            System.out.println();
        }
    }

    public static boolean isValidName(String name) {
        return NAME_PATTERN.matcher(name).matches();
    }
}
