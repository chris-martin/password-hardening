package seclogin;

/** Measurement statistics for a particular user-feature pair. */
public class MeasurementStats {

    private final double mean;
    private final double stDev;
    private final double missingValuesPercentage;

    public MeasurementStats(double mean, double stDev, double missingValuesPercentage) {
        this.mean = mean;
        this.stDev = stDev;
        this.missingValuesPercentage = missingValuesPercentage;
    }

    public double mean() {
        return mean;
    }

    public double stDev() {
        return stDev;
    }

    public double missingValuesPercentage() {
        return missingValuesPercentage;
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
