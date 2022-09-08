package one.paro.sharepicture.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    private final int port;
    private ServerSocket socket;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            socket = new ServerSocket(port);
            System.out.println("Server is running on port " + port);
            while (!socket.isClosed()) {
                System.out.println("Waiting for clients...");
                Socket conn = socket.accept();
                System.out.println("A connection is made: " + conn);
                executor.execute(new Worker(conn));
            }
        } catch (SocketException ignored) {
            // this exception is thrown when `socket` is closed
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        try {
            socket.close();
            System.out.println("Server is closed");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
