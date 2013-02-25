package seclogin;

/** Measurement statistics for a particular user-feature pair. */
public class MeasurementStats {

    public final double mean;
    public final double stDev;
    public final double missingValuesPercentage;

    public MeasurementStats(double mean, double stDev, double missingValuesPercentage) {
        this.mean = mean;
        this.stDev = stDev;
        this.missingValuesPercentage = missingValuesPercentage;
    }

    @Override
    public String toString() {
        return "MeasurementStats{" +
                "mean=" + mean +
                ", stDev=" + stDev +
                ", missingValuesPercentage=" + missingValuesPercentage +
                '}';
    }
}
