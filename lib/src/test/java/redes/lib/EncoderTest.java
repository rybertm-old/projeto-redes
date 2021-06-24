package redes.lib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EncoderTest {
    
    @Test
    public void testEncodeCaesar() {
        String cipheredMessage = Encoder.encodeCaesar("when i find myself in times of trouble", 5);
        assertEquals("bmjs n knsi rdxjqk ns ynrjx tk ywtzgqj", cipheredMessage);
        cipheredMessage = Encoder.encodeCaesar("mother mary comes to me", 29);
        assertEquals("prwkhu pdub frphv wr ph", cipheredMessage);
        cipheredMessage = Encoder.encodeCaesar("speaking words of wisdom", 18);
        assertEquals("khwscafy ogjvk gx oakvge", cipheredMessage);
    }

    @Test
    public void testDecodeCaesar() {
        int offset = 5;
        String message = "and in my hour of darkness";
        String cipheredMessage = Encoder.encodeCaesar(message, offset);
        String decodedMessage = Encoder.decodeCaesar(cipheredMessage, offset);
        assertEquals(message, decodedMessage);
        offset = 18;
        message = "she is standing right in front of me";
        cipheredMessage = Encoder.encodeCaesar(message, offset);
        decodedMessage = Encoder.decodeCaesar(cipheredMessage, offset);
        assertEquals(message, decodedMessage);
        offset = 31;
        message = "speaking words of wisdom";
        cipheredMessage = Encoder.encodeCaesar(message, offset);
        decodedMessage = Encoder.decodeCaesar(cipheredMessage, offset);
        assertEquals(message, decodedMessage);
    }
}
