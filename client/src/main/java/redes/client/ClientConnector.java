package redes.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ClientConnector {

    private static final int DEFAULT_PORT = 6868;
    private static final String DEFAULT_HOST = "localhost";

    /**
     * Retrieves and sends the specified image to the localhost server at the given
     * port and prints the response until a 'bye' is received
     * 
     * @param imagePath The path to the image which will be sent
     * @param port      The port which the server is listening
     */
    public void testImage(String imagePath, String host, Integer port) {
        if (port == null) {
            port = DEFAULT_PORT;
        }
        if (host == null) {
            host = DEFAULT_HOST;
        }
        var file = getFileFromPathString(imagePath);
        try (var fileStream = new FileInputStream(file); var socket = new Socket(host, port)) {
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

            while (true) {
                line = reader.readLine();
                if (line.equals("bye")) {
                    break;
                }
                System.out.println(line);
            }
        } catch (ConnectException ex) {
            System.err.println("Could not connect to the server: " + ex.getMessage() + "\nMaybe the port is wrong?");
        } catch (UnknownHostException ex) {
            System.err.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
            System.err.println("I/O error: " + ex.getMessage());
        }
    }

    /**
     * Retrieves the file specified by the {@code String} path
     * 
     * @param pathString The path of the file
     * @return The {@code File} retrieved
     */
    private File getFileFromPathString(String pathString) {
        if (pathString == null) {
            System.err.println("Please provide a image");
            System.exit(1);
        }
        Path path = Paths.get(pathString);
        return path.toFile();
    }
}
