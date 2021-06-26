package redes.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.primitives.Bytes;

import org.apache.commons.lang3.ArrayUtils;

import redes.lib.Chunk;
import redes.lib.Encoder;
import redes.lib.EncodingOptions;
import redes.lib.Png;

public class ServerConnection extends Thread {

    /**
     * Client identification
     */
    final String id;

    /**
     * Connections socket
     */
    final Socket socket;

    /**
     * Concurrent connections counter
     */
    final AtomicInteger connectionsCounter;

    public ServerConnection(Socket socket, AtomicInteger connectionsCounter) {
        this.id = socket.getInetAddress().toString();
        this.socket = socket;
        this.connectionsCounter = connectionsCounter;
    }

    @Override
    public void run() {
        listen();
    }

    /**
     * Handles the communication for this connection
     */
    private void listen() {
        try {
            // Opens communication streams
            var input = socket.getInputStream();
            var dis = new DataInputStream(input);
            var writer = new PrintWriter(socket.getOutputStream(), true);

            // Reads the file name and size
            var fileName = dis.readUTF();
            System.out.println(String.format("CLIENT %s - Received file: %s", id, fileName));
            final var fileSize = dis.readLong();
            var actualSize = fileSize;
            System.out.println(String.format("CLIENT %s - File size: %d", id, fileSize));

            // Creates buffer and data list
            final var BUFFER_SIZE = 8192;
            var buffer = new byte[BUFFER_SIZE];
            List<Byte> data = new ArrayList<>();
            System.out.println(String.format("CLIENT %s - Reading file", id));

            var read = 0;
            long total = 0;

            // Reads data input stream adding it to data list
            while ((read = dis.read(buffer, 0, (int) Math.min(actualSize, buffer.length))) > 0) {
                data.addAll(Bytes.asList(buffer).subList(0, read));
                total += read;
                System.out.println(String.format("CLIENT %s - Read: %d/%d - %.1f%%", id, total, fileSize,
                        (double) total / fileSize * 100));
                actualSize -= read;
            }

            var png = Png.fromBytes(data);

            String message = getPngMessage(png);

            if (message != null) {
                writer.println("There's a hidden message: ");
                writer.println(message);
            } else {
                writer.println("There's no hidden message");
            }

            // Tells client that the connection will end
            writer.println("bye");

            socket.close();

            // Decrements connections counter
            connectionsCounter.decrementAndGet();
        } catch (IOException | NullPointerException ex) {
            System.out.println("Server exception when processing data: " + ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println(String.format("CLIENT %s - Done", id));
    }

    /**
     * Checks if the given {@code Png} has a hidden message.
     * If true, decrypts and returns it
     * @param png The {@code Png} to be checked
     * @return The hidden message decrypted if it exists, otherwise {@code null}
     */
    private static String getPngMessage(Png png) {
        // reDe chunk type is used for hidden message
        Optional<Chunk> chunkOpt = png.chunkByType("reDe");
        if (chunkOpt.isPresent()) {
            var chunk = chunkOpt.get();

            // First byte contains encryption details
            var firstByte = chunk.getData().get(0);

            var messageByteList = chunk.getData().subList(1, chunk.getData().size());
            var messageByteArray = ArrayUtils.toPrimitive(messageByteList.toArray(new Byte[0]));

            // XOR encryption uses the first byte as 0b00000000
            if (firstByte.intValue() > 0) {
                // CAESAR encrypted
                // Ignoring the last 3 bits and shifting the first bytes's value
                // to discover the offset used in the message's encryption
                int offset = (int) (firstByte & 0b11111000);
                offset = offset >> 3;
                return Encoder.decode(new String(messageByteArray), EncodingOptions.CAESAR, offset);
            } else {
                // XOR encrypted
                return Encoder.decode(new String(messageByteArray), EncodingOptions.XOR);
            }
        }
        return null;
    }
}
