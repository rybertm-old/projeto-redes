package redes.lib;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * An encoder utility class
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Encoder {

    private static String key = "projetoRedes";

    public static String encode(String message, String encodeOption, Integer offset) {
        var newMessage = "";
        switch (EncodingOptions.getEnum(encodeOption)) {
            case CAESAR:
                newMessage = encodeCaesar(message, offset);
                break;
            case XOR:
                newMessage = encodeXOR(message);
                break;
            default:
                break;
        }

        return newMessage;
    }

    public static String encode(String message, String encodeOption) {
        return encode(message, encodeOption, null);
    }

    public static String encode(String message, EncodingOptions encodeOption) {
        return encode(message, encodeOption.toString(), null);
    }

    public static String encode(String message, EncodingOptions encodeOption, Integer offset) {
        return encode(message, encodeOption.toString(), offset);
    }

    public static String decode(String message, String encodeOption, Integer offset) {
        var newMessage = "";
        switch (EncodingOptions.getEnum(encodeOption)) {
            case CAESAR:
                newMessage = decodeCaesar(message, offset);
                break;
            case XOR:
                newMessage = decodeXOR(message);
                break;
            default:
                break;
        }

        return newMessage;
    }

    public static String decode(String message, EncodingOptions encodeOption, Integer offset) {
        return decode(message, encodeOption.toString(), offset);
    }

    public static String decode(String message, EncodingOptions encodeOption) {
        return decode(message, encodeOption.toString(), null);
    }

    public static String decode(String message, String encodeOption) {
        return decode(message, encodeOption, null);
    }

    /**
     * Encodes a {@code String} using Caesar Cipher with the given offset
     * 
     * @param message The {@code String} to be ciphered. Must contain only
     *                lower-cased letters or spaces
     * @param offset  An {@code int} representing the shift between each original
     *                and its respective modified character
     * @return The ciphered {@code String}
     */
    private static String encodeCaesar(String message, int offset) {
        var result = new StringBuilder();
        for (char character : message.toCharArray()) {
            if (Character.isLetter(character)) {
                char startLetter = Character.isUpperCase(character) ? 'A' : 'a';
                int originalAlphabetPosition = character - startLetter;
                int newAlphabetPosition = (originalAlphabetPosition + offset) % 26;
                var newCharacter = (char) (startLetter + newAlphabetPosition);
                result.append(newCharacter);
            } else {
                result.append(character);
            }
        }
        return result.toString();
    }

    /**
     * Decodes a Caesar-ciphered {@code String} with the given offset
     * 
     * @param cipheredMessage The {@code String} to be decoded
     * @param offset          An {@code int} representing the shift between each
     *                        modified and its respective original character
     * @return The decoded {@code String}
     */
    private static String decodeCaesar(String cipheredMessage, int offset) {
        return encodeCaesar(cipheredMessage, 26 - (offset % 26));
    }

    /**
     * Encodes a message using a simple XOR encoder
     * 
     * @param message to be encoded
     * @return The encoded message
     */
    private static String encodeXOR(String message) {
        byte idx = 0;
        byte[] msgBytes = message.getBytes();
        byte[] keyBytes = key.getBytes();
        var newMessage = new byte[message.length()];

        for (var i = 0; i < message.length(); i++) {
            if (idx == key.length()) {
                idx = 0;
            }
            newMessage[i] = (byte) (msgBytes[i] ^ keyBytes[idx++]);
        }

        return new String(newMessage);
    }

    /**
     * Decodes a message using a simple XOR decoder
     * 
     * @param message to be decoded
     * @return The decoded message
     */
    private static String decodeXOR(String message) {
        return encodeXOR(message);
    }
}
