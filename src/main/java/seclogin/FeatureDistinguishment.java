package seclogin;

public enum FeatureDistinguishment {
    ALPHA, BETA;

    public static byte asByte(FeatureDistinguishment feature) {
        return feature == null ? 0 : (byte) (feature.ordinal() + 1);
    }

    public static FeatureDistinguishment fromByte(byte b) {
        return b == 0 ? null : values()[b - 1];
    }
}
