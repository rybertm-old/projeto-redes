/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package redes.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import one.util.streamex.StreamEx;

public class ChunkTest {
    private static Stream<Byte> dataInBytes;

    private static Chunk test_chunk() {
        var chunkType = "RuSt";
        var data = "This is where your secret message will be!";
        return chunkFromStrings(chunkType, data);
    }

    public static List<Chunk> testingChunks() {
        List<Chunk> chunks = new ArrayList<>();
        chunks.add(chunkFromStrings("FrSt", "I am the first chunk"));
        chunks.add(chunkFromStrings("miDl", "I am another chunk"));
        chunks.add(chunkFromStrings("LASt", "I am the last chunk"));
        return chunks;
    }

    private static Chunk chunkFromStrings(String chunkType, String data) {
        int crc = Integer.parseUnsignedInt("2882656334");
        List<Byte> dataInBytes = data.chars().mapToObj(i -> (byte) i).collect(Collectors.toList());
        int length = Math.toIntExact(dataInBytes.size());
        List<Byte> chunk = StreamEx.of(ChunkHelper.fromIntToBytes(length))
                .append(chunkType.chars().mapToObj(i -> (byte) i)).append(dataInBytes)
                .append(ChunkHelper.fromIntToBytes(crc)).toList();

        return Chunk.fromBytes(chunk);
    }

    @Test
    public void testValidChunkFromBytes() {
        int length = 42;
        var chunkType = "RuSt";
        var data = "This is where your secret message will be!";
        int crc = Integer.parseUnsignedInt("2882656334");

        List<Byte> chunkBytes = StreamEx.of(ChunkHelper.fromIntToBytes(length))
                .append(chunkType.chars().mapToObj(i -> (byte) i)).append(data.chars().mapToObj(i -> (byte) i))
                .append(ChunkHelper.fromIntToBytes(crc)).toList();

        var chunk = Chunk.fromBytes(chunkBytes);

        Integer expectedLength = 42;
        String expectedType = "RuSt";
        Integer expectedCrc = Integer.parseUnsignedInt("2882656334");
        String expectedString = "This is where your secret message will be!";

        assertEquals("Invalid length", expectedLength, chunk.getLength());
        assertEquals("Invalid type", expectedType, chunk.getChunkType().toString());
        assertEquals("Invalid crc", expectedCrc, chunk.getCrc());
        assertEquals("Invalid message", expectedString, chunk.toString());
    }

    @Test
    public void testInvalidChunkFromBytes() {
        int length = 42;
        var chunkType = "RuSt";
        var data = "This is where your secret message will be!";
        int crc = Integer.parseUnsignedInt("2882656333");

        List<Byte> chunkBytes = StreamEx.of(ChunkHelper.fromIntToBytes(length))
                .append(chunkType.chars().mapToObj(i -> (byte) i)).append(data.chars().mapToObj(i -> (byte) i))
                .append(ChunkHelper.fromIntToBytes(crc)).toList();

        assertThrows(InvalidParameterException.class, () -> Chunk.fromBytes(chunkBytes));
    }

    @Test
    public void testChunkLength() {
        Chunk chunk = test_chunk();
        Integer length = 42;
        assertEquals("Invalid length", length, chunk.getLength());
    }

    @Test
    public void testChunkType() {
        Chunk chunk = test_chunk();
        assertEquals("Invalid type", "RuSt", chunk.getChunkType().toString());
    }

    @Test
    public void testChunkCrc() {
        Chunk chunk = test_chunk();
        Integer expected = Integer.parseUnsignedInt("2882656334");
        assertEquals("Invalid crc", expected, chunk.getCrc());
    }

    @Test
    public void testChunkToString() {
        Chunk chunk = test_chunk();
        String str = chunk.toString();
        assertEquals("This is where your secret message will be!", str);
    }
}
