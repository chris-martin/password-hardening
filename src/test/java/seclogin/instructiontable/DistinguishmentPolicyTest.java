package seclogin.instructiontable;

import org.junit.Assert;
import org.junit.Test;
import seclogin.MeasurementParams;

import static seclogin.instructiontable.Distinguishment.ALPHA;
import static seclogin.instructiontable.Distinguishment.BETA;

public class DistinguishmentPolicyTest {


    @Test
    public void testMeasurementDistinguishment() throws Exception {
        MeasurementParams[] measurementParams = new MeasurementParams[]{
                new MeasurementParams(10.0, 2.0),
                new MeasurementParams(10.0, 2.0),
                new MeasurementParams(10.0, 2.0)
        };
        DistinguishmentPolicy policy = new DistinguishmentPolicy(measurementParams);

        double[] measurements = new double[] {
                9.9999, // expect alpha
                10.0, // expect beta
                10.0001 // expect beta
        };

        Distinguishment[] expected = new Distinguishment[] {
                ALPHA,
                BETA,
                BETA
        };

        Assert.assertArrayEquals(expected, policy.measurmentDistinguishment(measurements));
    }
}
