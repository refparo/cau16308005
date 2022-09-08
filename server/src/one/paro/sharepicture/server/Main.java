package one.paro.sharepicture.server;

public class Main {
    public static void main(String[] args) {
        int port = 30110;
        if (args.length >= 2 && args[0].equals("-p")) try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException ignored) {
            // args[1] is not a number
        }
        new Server(port).run();
    }
}
