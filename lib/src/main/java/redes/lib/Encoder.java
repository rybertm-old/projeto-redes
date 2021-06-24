package redes.lib;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class Encoder {

    /**
     * Encodes a {@code String} using Caesar Cipher with the given offset
     * @param message The {@code String} to be ciphered. Must contain only lower-cased letters or spaces
     * @param offset An {@code int} representing the shift between
     * each original and its respective modified character
     * @return The ciphered {@code String}
     */
    public static String encodeCaesar(String message, int offset) {
        StringBuilder result = new StringBuilder();
        for (char character : message.toCharArray()) {
            if (character != ' ') {
                int originalAlphabetPosition = character - 'a';
                int newAlphabetPosition = (originalAlphabetPosition + offset) % 26;
                char newCharacter = (char) ('a' + newAlphabetPosition);
                result.append(newCharacter);
            } else {
                result.append(character);
            }
        }
        return result.toString();
    }

    /**
     * Decodes a Caesar-ciphered {@code String} with the given offset
     * @param cipheredMessage The {@code String} to be decoded
     * @param offset An {@code int} representing the shift between
     * each modified and its respective original character
     * @return The decoded {@code String}
     */ 
    public static String decodeCaesar(String cipheredMessage, int offset) {
        return encodeCaesar(cipheredMessage, 26 - (offset % 26));
    }
}
