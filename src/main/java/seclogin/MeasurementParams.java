package seclogin;

class MeasurementParams {

    private final double responseMean;

    private final double stDevMultiplier;

    /**
     * @param responseMean response mean
     * @param stDevMultiplier standard deviation multiplier
     */
    MeasurementParams(double responseMean, double stDevMultiplier) {
        this.responseMean = responseMean;
        this.stDevMultiplier = stDevMultiplier;
    }

    public double responseMean() {
        return responseMean;
    }

    public double stDevMultiplier() {
        return stDevMultiplier;
    }

}