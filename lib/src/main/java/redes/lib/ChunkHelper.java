package redes.lib;

import java.util.Arrays;
import java.util.List;

public class ChunkHelper {
    private ChunkHelper() {
    }

    public static Integer fromBytesToInt(List<Byte> bytes) {
        var array = bytes.toArray(new Byte[0]);
        int n3 = (array[0] << 24) & Integer.rotateRight(0xFF, 8);
        int n2 = (array[1] << 16) & Integer.rotateLeft(0xFF, 16);
        int n1 = (array[2] << 8) & Integer.rotateLeft(0xFF, 8);
        int n0 = array[3];

        return n3 | n2 | n1 | n0;
    }

    public static List<Byte> fromIntToBytes(Integer value) {
        var b = new Byte[] { 0, 0, 0, 0 };
        int n3 = value & 0xFF000000;
        int n2 = value & 0x00FF0000;
        int n1 = value & 0x0000FF00;
        int n0 = value & 0x000000FF;

        b[0] = (byte) (n3 >> 24);
        b[1] = (byte) (n2 >> 16);
        b[2] = (byte) (n1 >> 8);
        b[3] = (byte) n0;

        return Arrays.asList(b);
    }
}