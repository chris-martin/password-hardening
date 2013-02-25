package seclogin;

/**
 * System-wide parameters for a particular feature that govern whether
 * measurements of the feature for a particular user distinguish that user
 * with respect to that feature.
 */
public class MeasurementParams {

    public final double responseMean;

    public final double stDevMultiplier;

    /**
     * @param responseMean response mean
     * @param stDevMultiplier standard deviation multiplier
     */
    public MeasurementParams(double responseMean, double stDevMultiplier) {
        this.responseMean = responseMean;
        this.stDevMultiplier = stDevMultiplier;
    }

}
