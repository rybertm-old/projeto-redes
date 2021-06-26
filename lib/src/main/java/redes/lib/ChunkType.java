package redes.lib;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * The type of a chunk
 */
@EqualsAndHashCode
public class ChunkType {
    /**
     * Invalid chunk type error message
     */
    private static final String INVALID_TYPE = "Invalid chunk type";
    /**
     * Invalid chunk size error message
     */
    private static final String INVALID_SIZE = "Invalid chunk type size";

    /**
     * The chunk type data(4 {@code bytes})
     */
    @Getter
    private List<Byte> chunk = new ArrayList<>(Arrays.asList(new Byte[4]));

    /**
     * Creates a new ChunkType object
     */
    private ChunkType(List<Byte> chunk) {
        this.chunk = chunk;
    }

    /**
     * Creates a {@link ChunkType} from a string
     * 
     * @param chunk The chunk type as a string
     * @return The chunk type object
     * @throws InvalidParameterException If the chunk type is not valid
     */
    public static ChunkType fromString(String chunk) throws InvalidParameterException {
        if (chunk.length() == 4) {
            return fromByteArray(chunk.getBytes());
        } else {
            throw new InvalidParameterException(ChunkType.INVALID_SIZE);
        }
    }

    /**
     * Creates a {@link ChunkType} from a list of {@code bytes}
     * 
     * @param chunk The chunk type as a list of {@code bytes}
     * @return The chunk type object
     * @throws InvalidParameterException If the chunk type is not valid
     */
    public static ChunkType fromBytes(List<Byte> chunk) throws InvalidParameterException {
        if (chunk.size() == 4) {
            if (chunk.stream().allMatch(ChunkType::isAsciiAlphabetic) && ChunkType.isConforming(chunk)) {
                return new ChunkType(chunk);
            } else {
                throw new InvalidParameterException(ChunkType.INVALID_TYPE);
            }
        } else {
            throw new InvalidParameterException(ChunkType.INVALID_SIZE);
        }
    }

    /**
     * Creates a {@link ChunkType} from a array of {@code bytes}
     * 
     * @param chunk The chunk type as a array of {@code bytes}
     * @return The chunk type object
     * @throws InvalidParameterException If the chunk type is not valid
     */
    private static ChunkType fromByteArray(byte[] chunk) throws InvalidParameterException {
        List<Byte> ch = new ArrayList<>(Arrays.asList(new Byte[4]));
        for (var i = 0; i < chunk.length; i++) {
            ch.set(i, chunk[i]);
        }

        if (ch.stream().allMatch(ChunkType::isAsciiAlphabetic) && ChunkType.isConforming(ch)) {
            return new ChunkType(ch);
        } else {
            throw new InvalidParameterException(ChunkType.INVALID_TYPE);
        }
    }

    /**
     * Checks if a byte is a valid {@code ASCII} alphabetic character
     * 
     * @param value The byte to be checked
     * @return {@code true} If the byte is a valid {@code ASCII} alphabetic
     *         character, {@code false} otherwise
     */
    public static boolean isAsciiAlphabetic(byte value) {
        return (value > 64 && value < 91) || (value > 96 && value < 123);
    }

    /**
     * Checks if a byte array is conforming, according to the PNG specification
     * 
     * @param value The byte to be checked
     * @return {@code true} If the byte is conforming, {@code false} otherwise
     */
    public static boolean isConforming(byte[] chunk) throws InvalidParameterException {
        if (chunk.length != 4) {
            throw new InvalidParameterException(ChunkType.INVALID_TYPE);
        }

        return (chunk[2] & 0x20) == 0;
    }

    /**
     * Checks if a list of byte is conforming, according to the PNG specification
     * 
     * @param value The list of bytes to be checked
     * @return {@code true} If the byte is conforming, {@code false} otherwise
     */
    public static boolean isConforming(List<Byte> chunk) throws InvalidParameterException {
        if (chunk.size() != 4) {
            throw new InvalidParameterException(ChunkType.INVALID_TYPE);
        }

        return (chunk.get(2) & 0x20) == 0;
    }

    /**
     * Returns a representation of the chunk type as a list of bytes
     * 
     * @return The chunk type representation as a list of bytes
     */
    public List<Byte> asBytes() {
        return this.chunk;
    }

    /**
     * Checks if the chunk type is valid
     * 
     * @return {@code true} If the chunk type is valid, {@code false} otherwise
     */
    public boolean isValid() {
        return chunk.stream().allMatch(ChunkType::isAsciiAlphabetic) && this.isReservedBitValid();
    }

    /**
     * Checks if the chunk type is critical, according to the PNG specification
     * 
     * @return {@code true} If the chunk type is critical, {@code false} otherwise
     */
    public boolean isCritical() {
        return (chunk.get(0) & 0x20) == 0;
    }

    /**
     * Checks if the chunk type is public, according to the PNG specification
     * 
     * @return {@code true} If the chunk type is public, {@code false} otherwise
     */
    public boolean isPublic() {
        return (chunk.get(1) & 0x20) == 0;
    }

    /**
     * Checks if the chunk type reserved bit is valid, according to the PNG
     * specification
     * 
     * @return {@code true} If the chunk type reserved bit is valid, {@code false}
     *         otherwise
     */
    public boolean isReservedBitValid() {
        return (chunk.get(2) & 0x20) == 0;
    }

    /**
     * Checks if the chunk type is safe to copy, according to the PNG specification
     * 
     * @return {@code true} If the chunk type is safe to copy, {@code false}
     *         otherwise
     */
    public boolean isSafeToCopy() {
        return (chunk.get(3) & 0x20) > 0;
    }

    public String toString() {
        var builder = new StringBuilder();
        builder.append((char) chunk.get(0).byteValue());
        builder.append((char) chunk.get(1).byteValue());
        builder.append((char) chunk.get(2).byteValue());
        builder.append((char) chunk.get(3).byteValue());
        return builder.toString();
    }
}
