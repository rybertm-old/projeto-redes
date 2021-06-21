package redes.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Paths;

public class Client {
    public static void main(String[] args) {
        var port = 6868;
        if (args.length < 1) {
            System.err.println("Please provide a image");
            return;
        }
        var path = Paths.get(args[0]);

        if (args.length > 1) {
            port = Integer.parseInt(args[1]);
        }

        var file = path.toFile();
        try (var fileStream = new FileInputStream(file); var socket = new Socket("localhost", port)) {
            var output = socket.getOutputStream();
            var dos = new DataOutputStream(output);

            final var BUFFER_SIZE = 8192;
            var buffer = new byte[BUFFER_SIZE];
            var read = 0;

            // Send file name
            dos.writeUTF(file.getName());
            // Send file length
            dos.writeLong(file.length());
            // Read file chunk by chunk and add it to the socket output stream
            while ((read = fileStream.read(buffer)) > 0) {
                dos.write(buffer, 0, read);
            }
            System.out.println("File read");
            // Send data
            System.out.println("File sent");

            var input = socket.getInputStream();
            var reader = new BufferedReader(new InputStreamReader(input));

            var line = "";

            while (!line.equals("bye")) {
                line = reader.readLine();
                System.out.println(line);
            }
        } catch (UnknownHostException ex) {
            System.err.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("I/O error: " + ex.getMessage());
        }
    }
}