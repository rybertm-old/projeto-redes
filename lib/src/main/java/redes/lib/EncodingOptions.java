package redes.lib;

/**
 * The supported encoding options
 */
public enum EncodingOptions {
    CAESAR, XOR;

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
        throw new IllegalArgumentException();
    }
}
