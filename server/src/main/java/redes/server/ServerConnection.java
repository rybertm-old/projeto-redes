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
    final String id;
    final Socket socket;
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

    private void listen() {
        try {
            var input = socket.getInputStream();
            var dis = new DataInputStream(input);
            var writer = new PrintWriter(socket.getOutputStream(), true);

            final var BUFFER_SIZE = 8192;
            var fileName = dis.readUTF();
            System.out.println(String.format("CLIENT %s - Received file: %s", id, fileName));

            final var fileSize = dis.readLong();
            var size = fileSize;
            System.out.println(String.format("CLIENT %s - File size: %d", id, fileSize));

            var read = 0;
            long total = 0;

            var buffer = new byte[BUFFER_SIZE];
            List<Byte> data = new ArrayList<>();
            System.out.println(String.format("CLIENT %s - Reading file", id));

            while ((read = dis.read(buffer, 0, (int) Math.min(size, buffer.length))) > 0) {
                data.addAll(Bytes.asList(buffer).subList(0, read));
                total += read;
                System.out.println(String.format("CLIENT %s - Read: %d/%d - %.1f%%", id, total, fileSize,
                        (double) total / fileSize * 100));
                size -= read;
            }

            var png = Png.fromBytes(data);

            String message = getPngMessage(png);

            if (message != null) {
                writer.println("There's a hidden message: ");
                writer.println(message);
            }

            writer.println("bye");

            socket.close();
            connectionsCounter.decrementAndGet();
        } catch (IOException | NullPointerException ex) {
            System.out.println("Server exception when processing data: " + ex.getMessage());
            ex.printStackTrace();
        }
        System.out.println(String.format("CLIENT %s - Done", id));
    }

    private static String getPngMessage(Png png) {
        Optional<Chunk> chunkOpt = png.chunkByType("reDe");
        if (chunkOpt.isPresent()) {
            var chunk = chunkOpt.get();
            var firstByte = chunk.getData().get(0);

            var messageByteList = chunk.getData().subList(1, chunk.getData().size());
            var messageByteArray = ArrayUtils.toPrimitive(messageByteList.toArray(new Byte[0]));

            if (firstByte.intValue() > 0) {
                // CAESAR encrypted
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
