package seclogin;

class MeasurementStats {

    private final double mean;

    private final double stDev;

    public MeasurementStats(double mean, double stDev) {
        this.mean = mean;
        this.stDev = stDev;
    }

    public double mean() {
        return mean;
    }

    public double stDev() {
        return stDev;
    }

}
