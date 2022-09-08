package one.paro.sharepicture.client;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Scanner;
import javax.imageio.ImageIO;

public class TUI implements Runnable {
    private final Scanner sc = new Scanner(System.in);
    private Client client;

    @Override
    public void run() {
        while (true) {
            System.out.print("> ");
            String[] cmd = sc.nextLine().split("\\s+");
            if (cmd.length == 0) break;
            switch (cmd[0].toLowerCase()) {
            case "connect":
                if (cmd.length != 3) {
                    System.out.println("Syntax error!");
                    break;
                }
                String hostname = cmd[1];
                int port;
                try {
                    port = Integer.parseInt(cmd[2]);
                } catch (NumberFormatException e) {
                    System.out.println("Syntax error!");
                    break;
                }
                client = new Client(hostname, port);
                new Thread(client).start();
                break;
            case "list":
                if (cmd.length != 1) {
                    System.out.println("Syntax error!");
                    break;
                }
                if (client == null) {
                    System.out.println("Please connect first!");
                    break;
                }
                String[] filenames = client.list();
                if (filenames == null) {
                    System.out.println("Server error!");
                    client = null;
                    break;
                }
                for (String filename : filenames) {
                    System.out.println(filename);
                }
                break;
            case "show":
                if (cmd.length != 2) {
                    System.out.println("Syntax error!");
                    break;
                }
                if (client == null) {
                    System.out.println("Please connect first!");
                    break;
                }
                BufferedImage img = client.show(cmd[1]);
                if (img == null) {
                    System.out.println("Couldn't get picture!");
                    break;
                }
                File downloadsDir = new File(System.getProperty("user.dir"), "downloads");
                downloadsDir.mkdirs();
                if (!downloadsDir.isDirectory()) {
                    System.out.println("Couldn't create downloads directory!");
                    break;
                }
                try {
                    ImageIO.write(img, "png", new File(downloadsDir, cmd[1]));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "close":
                if (cmd.length != 1) {
                    System.out.println("Syntax error!");
                    break;
                }
                if (client == null) {
                    System.out.println("Please connect first!");
                    break;
                }
                client.close();
                client = null;
                break;
            default:
            }
        }
    }
}
