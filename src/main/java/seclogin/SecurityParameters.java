package seclogin;

/** Security parameters of the scheme. */
public class SecurityParameters {

    public static final int Q_LEN = 160;    // bit length of q
    public static final int R_LEN = 160;    // bit length of r

    /**
     * Lower bound (exclusive) on the percentage of measurements for a particular feature declined by the user
     * at which that feature will be considered non-distinguishing.
     */
    public static final double DECLINED_MEASUREMENT_NON_DISTINGUISHMENT_THRESHOLD = 0.5;

    private SecurityParameters() {}
}
