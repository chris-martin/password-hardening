package seclogin;

/** Measurement statistics for a particular user-feature pair. */
public class MeasurementStats {

    public final double mean;
    public final double stDev;

    public MeasurementStats(double mean, double stDev) {
        this.mean = mean;
        this.stDev = stDev;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MeasurementStats that = (MeasurementStats) o;

        if (Double.compare(that.mean, mean) != 0) return false;
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
        return result;
    }

    @Override
    public String toString() {
        return String.format("{mean=%.3f, stDev=%.3f}", mean, stDev);
    }
}
