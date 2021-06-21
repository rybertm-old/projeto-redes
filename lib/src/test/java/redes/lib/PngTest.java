package redes.lib;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class PngTest {
    @Test
    public void testValidPngFromChunks() {
        List<Chunk> chunks = ChunkTest.testingChunks();
        Png png = new Png(chunks);
        int expected = 3;
        int actual = png.getChunks().size();
        assertEquals(expected, actual);
    }
    
}
