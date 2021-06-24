package redes.lib;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class EncoderTest {
    
    @Test
    public void testEncodeCaesar() {
        String cipheredMessage = Encoder.encodeCaesar("wHen I find myself* in times of Trouble,", 5);
        assertEquals("bMjs N knsi rdxjqk* ns ynrjx tk Ywtzgqj,", cipheredMessage);
        cipheredMessage = Encoder.encodeCaesar("mother %Mary comes to mE", 29);
        assertEquals("prwkhu %Pdub frphv wr pH", cipheredMessage);
        cipheredMessage = Encoder.encodeCaesar("speaking wOrds. of Wisdom", 18);
        assertEquals("khwscafy oGjvk. gx Oakvge", cipheredMessage);
    }

    @Test
    public void testDecodeCaesar() {
        int offset = 5;
        String message = "And in My hour of #darkness,";
        String cipheredMessage = Encoder.encodeCaesar(message, offset);
        String decodedMessage = Encoder.decodeCaesar(cipheredMessage, offset);
        assertEquals(message, decodedMessage);
        offset = 18;
        message = "$She iS (standIng) right in fRoNt of me";
        cipheredMessage = Encoder.encodeCaesar(message, offset);
        decodedMessage = Encoder.decodeCaesar(cipheredMessage, offset);
        assertEquals(message, decodedMessage);
        offset = 31;
        message = "speAking &Words oF wisdom";
        cipheredMessage = Encoder.encodeCaesar(message, offset);
        decodedMessage = Encoder.decodeCaesar(cipheredMessage, offset);
        assertEquals(message, decodedMessage);
    }
}
