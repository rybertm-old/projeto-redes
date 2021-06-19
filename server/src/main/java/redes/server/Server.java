package redes.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Bytes;

public class Server extends Thread {

    final long id;
    final Socket socket;

    public Server(long id, Socket socket) {
        this.id = id;
        this.socket = socket;
    }

    public static void main(String[] args) {
        var port = 6868;
        if (args.length >= 1)
            port = Integer.parseInt(args[0]);

        long id = 0;
        try (var serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);

            while (true) {
                var socket = serverSocket.accept();
                id += 1;
                System.out.println(String.format("CLIENT %d - Connected", id));

                new Server(id, socket).start();
            }
        } catch (IOException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }

    }

    @Override
    public void run() {
        listen();
    }

    private void listen() {
        try {
            var input = socket.getInputStream();
            var dis = new DataInputStream(input);

            var output = socket.getOutputStream();
            var writer = new PrintWriter(output, true);

            final var BUFFER_SIZE = 8192;
            var fileName = dis.readUTF();
            System.out.println(String.format("CLIENT %d - Received file: %s", id, fileName));

            final var fileSize = dis.readLong();
            var size = fileSize;
            System.out.println(String.format("CLIENT %d - File size: %d", id, fileSize));

            var read = 0;
            long total = 0;

            var buffer = new byte[BUFFER_SIZE];
            List<Byte> data = new ArrayList<>((int) fileSize);
            System.out.println(String.format("CLIENT %d - Reading file", id));

            while ((read = dis.read(buffer, 0, (int) Math.min(size, buffer.length))) > 0) {
                data.addAll(Bytes.asList(buffer));
                total += read;
                System.out.println(String.format("CLIENT %d - Read: %d/%d - %.1f%%", id, total, fileSize,
                        (double) total / fileSize * 100));
                size -= read;
            }
            writer.println("bye");

            socket.close();
        } catch (IOException | NullPointerException ex) {
            System.out.println("Server exception when processing data: " + ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println(String.format("CLIENT %d - Done", id));
    }

    private String readData(InputStream reader) throws IOException {
        // return reader.readLine(); // reads a line of text
        return null;
    }
}
