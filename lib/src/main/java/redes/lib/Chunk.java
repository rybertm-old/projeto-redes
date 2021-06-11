package redes.lib;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;

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
        type.addAll(data);
        var crcGen = new CRC32();
        crcGen.update(type.toString().getBytes());

        this.length = data.size();
        this.chunkType = chunkType;
        this.data = data;
        this.crc = (int) crcGen.getValue();
    }

    public static Chunk fromBytes(List<Byte> chunk) throws InvalidParameterException {
        Integer offset = 0;
        Integer length = ChunkHelper.fromBytesToInt(subList(chunk, 0, 4));
        offset += 4;

        var type = subList(chunk, offset, offset + 4);
        var chunkType = ChunkType.fromBytes(type);
        offset += 4;

        var data = subList(chunk, offset, offset + length);
        offset += length;

        int valueCrc = ChunkHelper.fromBytesToInt(subList(chunk, offset, offset + 4));

        type.addAll(data);
        var bytes = new byte[type.size()];
        for (var i = 0; i < type.size(); i++) {
            bytes[i] = type.get(i);
        }
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

    private static List<Byte> subList(List<Byte> data, int start, int end) {
        int size = end - start;
        if (size == 0) {
            return new ArrayList<>(1);
        }

        var list = new ArrayList<Byte>(size);

        for (int i = start; i < end; i++) {
            list.add(data.get(i));
        }

        return list;
    }

    public String toString() {
        var builder = new StringBuilder("");

        for (var ch : this.data) {
            builder.append((char) ch.byteValue());
        }

        return builder.toString();
    }
}
