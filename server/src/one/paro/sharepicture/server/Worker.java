package one.paro.sharepicture.server;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;

import one.paro.sharepicture.Method;
import one.paro.sharepicture.Status;

public class Worker implements Runnable {
    private static final File picturesDir =
        new File(System.getProperty("user.dir"), "pictures");
    private final Socket conn;

    Worker(Socket conn) {
        this.conn = conn;
    }

    @Override
    public void run() {
        try (
            DataInputStream in = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(conn.getOutputStream()))
        ) {
            boolean running = true;
            while (running) {
                switch (Method.valueOf(in.readUTF())) {
                case LIST:
                    picturesDir.mkdirs();
                    File[] files = picturesDir.listFiles(file -> file.isFile() && file.canRead());
                    if (files == null) {
                        out.writeUTF(Status.ERROR.name());
                        throw new RuntimeException("Cannot list pictures!");
                    }
                    System.out.println("Called to LIST, listing " + files.length + " pictures");
                    out.writeUTF(Status.OK.name());
                    out.writeInt(files.length);
                    for (File file : files) {
                        out.writeUTF(file.getName());
                    }
                    break;
                case SHOW:
                    String filename = in.readUTF();
                    File file = new File(picturesDir, filename);
                    if (!file.isFile() || !file.canRead()) {
                        System.out.println("Called to SHOW, but couldn't find " + filename);
                        out.writeUTF(Status.NOT_FOUND.name());
                        break;
                    }
                    System.out.println("Called to SHOW, sending " + filename);
                    out.writeUTF(Status.OK.name());
                    out.writeLong(file.length());
                    Files.copy(file.toPath(), out);
                    break;
                case CLOSE:
                    System.out.println("Called to CLOSE");
                    running = false;
                    break;
                default:
                    System.out.println("Called by an illegal method");
                    out.writeUTF(Status.ERROR.name());
                }
                out.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            conn.close();
            System.out.println("Connection " + conn + " is closed");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
