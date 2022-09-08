package one.paro.sharepicture.client;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import javax.imageio.ImageIO;

import one.paro.sharepicture.Method;
import one.paro.sharepicture.Status;

public class Client implements Runnable {
    private final Socket conn;
    private final BlockingQueue<String[]> requests = new ArrayBlockingQueue<>(5);
    private final BlockingQueue<Object[]> responses = new ArrayBlockingQueue<>(5);

    public Client(String hostname, int port) {
        try {
            this.conn = new Socket(hostname, port);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        try (
            DataInputStream in = new DataInputStream(new BufferedInputStream(conn.getInputStream()));
            DataOutputStream out = new DataOutputStream(new BufferedOutputStream(conn.getOutputStream()))
        ) {
            boolean running = true;
            while (running) try {
                String[] request = requests.take();
                for (String msg : request) {
                    out.writeUTF(msg);
                }
                out.flush();
                switch (Method.valueOf(request[0])) {
                case LIST:
                    switch (Status.valueOf(in.readUTF())) {
                    case OK:
                        int length = in.readInt();
                        String[] filenames = new String[length];
                        for (int i = 0; i < length; i++) {
                            filenames[i] = in.readUTF();
                        }
                        responses.add(new Object[]{
                            Status.OK,
                            filenames
                        });
                        break;
                    case ERROR:
                        responses.add(new Object[]{Status.ERROR});
                        running = false;
                        break;
                    default:
                        throw new IllegalStateException();
                    }
                    break;
                case SHOW:
                    switch (Status.valueOf(in.readUTF())) {
                    case OK:
                        long length = in.readLong();
                        FilterInputStream filter = new FilterInputStream(in) {
                            private long pos = 0;
                            @Override
                            public boolean markSupported() {
                                return false;
                            }
                            @Override
                            public int available() throws IOException {
                                return (int) Math.min(super.available(), length - pos);
                            }
                            @Override
                            public int read() throws IOException {
                                if (pos >= length) {
                                    return -1;
                                }
                                pos++;
                                return super.read();
                            }
                            @Override
                            public int read(byte[] b, int off, int len) throws IOException {
                                if (pos >= length) {
                                    return -1;
                                }
                                int count = super.read(b, off, (int) Math.min(len, length - pos));
                                if (count < 0) {
                                    return -1;
                                }
                                pos += count;
                                return count;
                            }
                            @Override
                            public long skip(long n) throws IOException {
                                if (pos >= length) {
                                    return -1;
                                }
                                long skipped = super.skip(Math.min(n, length - pos));
                                pos += skipped;
                                return skipped;
                            }
                            @Override
                            public void close() throws IOException {
                                while (pos < length) {
                                    skip(length - pos);
                                }
                            }
                        };
                        BufferedImage img = ImageIO.read(filter);
                        responses.add(new Object[]{Status.OK, img});
                        break;
                    case NOT_FOUND:
                        responses.add(new Object[]{Status.NOT_FOUND});
                        break;
                    default:
                        throw new IllegalStateException();
                    }
                    break;
                case CLOSE:
                    running = false;
                }
            } catch (InterruptedException e) {
                running = false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            conn.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] list() {
        try {
            requests.put(new String[]{Method.LIST.name()});
            Object[] response = responses.take();
            switch ((Status) response[0]) {
            case OK:
                return (String[]) response[1];
            case ERROR:
                return null;
            default:
                throw new IllegalStateException();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public BufferedImage show(String filename) {
        try {
            requests.put(new String[]{Method.SHOW.name(), filename});
            Object[] response = responses.take();
            switch ((Status) response[0]) {
            case OK:
                return (BufferedImage) response[1];
            case NOT_FOUND:
                return null;
            default:
                throw new IllegalStateException();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public void close() {
        try {
            requests.put(new String[]{Method.CLOSE.name()});
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
