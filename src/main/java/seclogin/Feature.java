package seclogin;

public enum Feature {

    ALPHA, BETA;

    public static byte asByte(Feature feature) {
        return feature == null ? 0 : (byte) (feature.ordinal() + 1);
    }

    public static Feature fromByte(byte b) {
        return b == 0 ? null : values()[b - 1];
    }
}
