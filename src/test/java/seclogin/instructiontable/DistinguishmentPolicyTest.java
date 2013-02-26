package seclogin.instructiontable;

import org.junit.Assert;
import org.junit.Test;
import seclogin.MeasurementParams;
import seclogin.MeasurementStats;

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

        Assert.assertArrayEquals(expected, policy.measurementDistinguishment(measurements));
    }

    @Test
    public void testUserDistinguishment() throws Exception {
        MeasurementParams[] measurementParams = new MeasurementParams[]{
                new MeasurementParams(10.0, 2.0),
                new MeasurementParams(10.0, 2.0),
                new MeasurementParams(10.0, 2.0),
                new MeasurementParams(10.0, 2.0),
                new MeasurementParams(10.0, 2.0)
        };
        DistinguishmentPolicy policy = new DistinguishmentPolicy(measurementParams);

        MeasurementStats[] stats = new MeasurementStats[]{
                null,
                new MeasurementStats(4.0, 3.0),
                new MeasurementStats(3.9999, 3.0),
                new MeasurementStats(15.0, 2.5),
                new MeasurementStats(15.0001, 2.5)
        };

        Distinguishment[] expected = new Distinguishment[] {
                null,
                null,
                ALPHA,
                null,
                BETA
        };

        Assert.assertArrayEquals(expected, policy.userDistinguishment(stats));
    }
}
