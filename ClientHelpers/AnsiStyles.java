package ClientHelpers;

public class AnsiStyles {
    public static final String RESET = "\033[0m";
    public static final String BOLD = "\033[1m";

    public static String bold(String text) {
        return BOLD + text + RESET;
    }
}
