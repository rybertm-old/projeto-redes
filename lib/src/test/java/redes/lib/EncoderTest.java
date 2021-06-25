package redes.lib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class EncoderTest {

    @Test
    public void testEncodeCaesar() {
        String cipheredMessage = Encoder.encode("wHen I find myself* in times of Trouble,", "caesar", 5);
        assertEquals("bMjs N knsi rdxjqk* ns ynrjx tk Ywtzgqj,", cipheredMessage);
        cipheredMessage = Encoder.encode("mother %Mary comes to mE", "caesar", 29);
        assertEquals("prwkhu %Pdub frphv wr pH", cipheredMessage);
        cipheredMessage = Encoder.encode("speaking wOrds. of Wisdom", EncodingOptions.CAESAR, 18);
        assertEquals("khwscafy oGjvk. gx Oakvge", cipheredMessage);
    }

    @Test
    public void testDecodeCaesar() {
        int offset = 5;
        String message = "And in My hour of #darkness,";
        String cipheredMessage = Encoder.encode(message, "caesar", offset);
        String decodedMessage = Encoder.decode(cipheredMessage, "caesar", offset);
        assertEquals(message, decodedMessage);
        offset = 18;
        message = "$She iS (standIng) right in fRoNt of me";
        cipheredMessage = Encoder.encode(message, "caesar", offset);
        decodedMessage = Encoder.decode(cipheredMessage, "caesar", offset);
        assertEquals(message, decodedMessage);
        offset = 31;
        message = "speAking &Words oF wisdom";
        cipheredMessage = Encoder.encode(message, "caesar", offset);
        decodedMessage = Encoder.decode(cipheredMessage, EncodingOptions.CAESAR, offset);
        assertEquals(message, decodedMessage);
    }

    @Test
    public void testDecodeXOR() {
        String message = "And in My hour of #darkness,";
        String cipheredMessage = Encoder.encode(message, "xor");
        String decodedMessage = Encoder.decode(cipheredMessage, "xor");
        assertEquals(message, decodedMessage);
        message = "$She iS (standIng) right in fRoNt of me";
        cipheredMessage = Encoder.encode(message, "xor");
        decodedMessage = Encoder.decode(cipheredMessage, "xor");
        assertEquals(message, decodedMessage);
        message = "speAking &Words oF wisdom";
        cipheredMessage = Encoder.encode(message, EncodingOptions.XOR);
        decodedMessage = Encoder.decode(cipheredMessage, EncodingOptions.XOR);
        assertEquals(message, decodedMessage);
    }
}
