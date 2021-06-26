package redes.lib;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

import org.apache.commons.lang3.ArrayUtils;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import one.util.streamex.StreamEx;

/**
 * A PNG image chunk representation
 */
@AllArgsConstructor
@EqualsAndHashCode
public class Chunk {
    private static final String INVALID_CHUNK = "Invalid chunk";

    /**
     * Length of the chunk
     */
    @Getter
    private Integer length;

    /**
     * The type of the chunk
     */
    @Getter
    private ChunkType chunkType;

    /**
     * The chunk data
     */
    @Getter
    private List<Byte> data;

    /**
     * The {@link CRC32} of the chunk
     */
    @Getter
    private Integer crc;

    /**
     * Creates a new Chunk object
     */
    public Chunk(ChunkType chunkType, List<Byte> data) {
        List<Byte> type = chunkType.getChunk();
        var list = StreamEx.of(type.stream()).append(data.stream()).toList();
        var bytes = ArrayUtils.toPrimitive(list.toArray(new Byte[0]));
        var crcGen = new CRC32();
        crcGen.update(bytes);

        this.length = data.size();
        this.chunkType = chunkType;
        this.data = data;
        this.crc = (int) crcGen.getValue();
    }

    /**
     * Creates a {@code Chunk} from a list of bytes
     * 
     * @param chunk Byte list
     * @return A valid chunk
     * @throws InvalidParameterException If a chunk is not valid
     */
    public static Chunk fromBytes(List<Byte> chunk) throws InvalidParameterException {
        Integer offset = 0;
        Integer length = ChunkHelper.fromBytesToInt(new ArrayList<>(chunk.subList(0, 4)));
        offset += 4;

        var type = new ArrayList<>(chunk.subList(offset, offset + 4));
        var chunkType = ChunkType.fromBytes(type);
        offset += 4;

        var data = new ArrayList<>(chunk.subList(offset, offset + length));
        offset += length;

        int valueCrc = ChunkHelper.fromBytesToInt(new ArrayList<>(chunk.subList(offset, offset + 4)));

        var byteList = StreamEx.of(type).append(data).toArray(Byte.class);
        var bytes = ArrayUtils.toPrimitive(byteList);
        var crcGen = new CRC32();
        crcGen.update(bytes);
        int crc = (int) crcGen.getValue();

        if (crc == valueCrc) {
            return new Chunk(length, chunkType, data, crc);
        } else {
            throw new InvalidParameterException(Chunk.INVALID_CHUNK);
        }
    }

    /**
     * Returns a representation of the chunk as a list of bytes
     * 
     * @return The chunk representation as a list of bytes
     */
    public List<Byte> asBytes() {
        return StreamEx.of(ChunkHelper.fromIntToBytes(this.length)).append(this.chunkType.asBytes()).append(this.data)
                .append(ChunkHelper.fromIntToBytes(this.crc)).toList();
    }

    public String toString() {
        var builder = new StringBuilder("");

        for (var ch : this.data) {
            builder.append((char) ch.byteValue());
        }

        return builder.toString();
    }
}
