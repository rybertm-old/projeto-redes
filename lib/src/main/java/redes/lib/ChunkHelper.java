package redes.lib;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

public class ChunkHelper {
    private ChunkHelper() {
    }

    public static Integer fromBytesToInt(List<Byte> bytes) {
        var temp = bytes.toArray(new Byte[0]);
        var buffer = ByteBuffer.wrap(ArrayUtils.toPrimitive(temp));
        buffer.order(ByteOrder.BIG_ENDIAN);

        return buffer.getInt();
    }

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