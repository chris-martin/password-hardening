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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MeasurementStats that = (MeasurementStats) o;

        if (Double.compare(that.mean, mean) != 0) return false;
        if (Double.compare(that.missingValuesPercentage, missingValuesPercentage) != 0) return false;
        if (Double.compare(that.stDev, stDev) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = mean != +0.0d ? Double.doubleToLongBits(mean) : 0L;
        result = (int) (temp ^ (temp >>> 32));
        temp = stDev != +0.0d ? Double.doubleToLongBits(stDev) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = missingValuesPercentage != +0.0d ? Double.doubleToLongBits(missingValuesPercentage) : 0L;
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
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
