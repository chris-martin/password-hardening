package seclogin.instructiontable;

import seclogin.MeasurementParams;

import java.util.Random;

public class RandomMeasurementParams {

    private final Random random;

    public RandomMeasurementParams(Random random) {
        this.random = random;
    }

    public MeasurementParams[] nextMeasurementParams(int len) {
        MeasurementParams[] params = new MeasurementParams[len];
        for (int i = 0; i < params.length; i++) {
            params[i] = new MeasurementParams(random.nextInt(20), 1.0);
        }
        return params;
    }
}
