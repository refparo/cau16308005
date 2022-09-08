package one.paro.sharepicture.client;

public class Main {
    public static void main(String[] args) {
        if (args.length >= 1 && args[0].equals("--tui")) {
            new TUI().run();
        } else {
            new GUI().run();
        }
    }
}
