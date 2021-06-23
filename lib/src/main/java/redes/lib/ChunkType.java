package redes.lib;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
public class ChunkType {
    private static final String INVALID_TYPE = "Invalid chunk type";
    private static final String INVALID_SIZE = "Invalid chunk type size";

    @Getter
    private List<Byte> chunk = new ArrayList<>(Arrays.asList(new Byte[4]));

    private ChunkType(List<Byte> chunk) {
        this.chunk = chunk;
    }

    public static ChunkType fromString(String chunk) throws InvalidParameterException {
        if (chunk.length() == 4) {
            return fromByteArray(chunk.getBytes());
        } else {
            throw new InvalidParameterException(ChunkType.INVALID_SIZE);
        }
    }

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

    public static boolean isAsciiAlphabetic(byte value) {
        return (value > 64 && value < 91) || (value > 96 && value < 123);
    }

    public static boolean isConforming(byte[] chunk) throws InvalidParameterException {
        if (chunk.length != 4) {
            throw new InvalidParameterException(ChunkType.INVALID_TYPE);
        }

        return (chunk[2] & 0x20) == 0;
    }

    public static boolean isConforming(List<Byte> chunk) throws InvalidParameterException {
        if (chunk.size() != 4) {
            throw new InvalidParameterException(ChunkType.INVALID_TYPE);
        }

        return (chunk.get(2) & 0x20) == 0;
    }

    public List<Byte> asBytes() {
        return this.chunk;
    }

    public boolean isValid() {
        return chunk.stream().allMatch(ChunkType::isAsciiAlphabetic) && this.isReservedBitValid();
    }

    public boolean isCritical() {
        return (chunk.get(0) & 0x20) == 0;
    }

    public boolean isPublic() {
        return (chunk.get(1) & 0x20) == 0;
    }

    public boolean isReservedBitValid() {
        return (chunk.get(2) & 0x20) == 0;
    }

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
