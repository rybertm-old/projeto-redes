package redes.lib;

import lombok.Getter;

/**
 * The supported encoding options
 */
public enum EncodingOptions {
    CAESAR(0b00000111), XOR(0b00000000);

    private EncodingOptions(Integer value) {
        this.value = Byte.valueOf(value.byteValue());
    }

    @Getter
    private Byte value;

    /**
     * Does the same as {@code Enum.valueOf}, most probably less efficiently, but it
     * normalizes the input beforehand
     */
    public static EncodingOptions getEnum(String value) throws IllegalArgumentException {
        for (var v : values()) {
            if (v.toString().equals(value.toUpperCase())) {
                return v;
            }
        }
        throw new IllegalArgumentException("Encryption not supported: " + value);
    }
}
