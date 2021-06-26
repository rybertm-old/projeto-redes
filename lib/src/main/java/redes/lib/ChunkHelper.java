package redes.lib;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

/**
 * Helper class to use when dealing with a png, chunk or chunk type object
 */
public class ChunkHelper {
    private ChunkHelper() {
    }

    /**
     * Converts a 4 {@code byte} list, in {@code big endian} order, to a 32-bit
     * integer
     * 
     * @param bytes 4 {@code byte} list to be converted
     * @return The {@code big endian} integer representation of the {@code byte}
     *         list
     */
    public static Integer fromBytesToInt(List<Byte> bytes) {
        var temp = bytes.toArray(new Byte[0]);
        var buffer = ByteBuffer.wrap(ArrayUtils.toPrimitive(temp));
        buffer.order(ByteOrder.BIG_ENDIAN);

        return buffer.getInt();
    }

    /**
     * Converts a 32-bit integer to a 4 {@code byte} list, in {@code big endian}
     * order
     * 
     * @param value Integer to be converted
     * @return The 4 {@code byte} list representation of the integer
     */
    public static List<Byte> fromIntToBytes(Integer value) {
        var buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putInt(value);
        var array = buffer.array();
        var list = new ArrayList<Byte>();

        for (byte item : array) {
            list.add(item);
        }

        return list;
    }
}