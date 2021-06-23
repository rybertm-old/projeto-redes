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

@AllArgsConstructor
@EqualsAndHashCode
public class Chunk {
    private static final String INVALID_CHUNK = "Invalid chunk";

    @Getter
    private Integer length;

    @Getter
    private ChunkType chunkType;

    @Getter
    private List<Byte> data;

    @Getter
    private Integer crc;

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
