package seclogin.instructiontable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seclogin.HardenedPassword;
import seclogin.MeasurementParams;
import seclogin.Password;
import seclogin.TestRandom;

import java.util.Random;

import static seclogin.instructiontable.InstructionTable.Entry.Column.ALPHA;
import static seclogin.instructiontable.InstructionTable.Entry.Column.BETA;

public class InstructionTableTest {

    private Random random;
    private RandomMeasurementParams randomMeasurementParams;

    @Before
    public void setUp() throws Exception {
        random = TestRandom.random();
        randomMeasurementParams = new RandomMeasurementParams(random);
    }

    @Test
    public void testGenerateWithoutStats() throws Exception {
        Password pwd = new Password("asdf");
        MeasurementParams[] measurementParams = randomMeasurementParams.nextMeasurementParams(3);
        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
                InstructionTable.generate(pwd, measurementParams, random);

        Assert.assertEquals(measurementParams.length, tableAndHpwd.table.table.length);
    }

    @Test
    public void testSelectColumn() throws Exception {
        Password pwd = new Password("asdf");
        MeasurementParams[] measurementParams = new MeasurementParams[]{
                new MeasurementParams(10.0, 2.0),
                new MeasurementParams(10.0, 2.0),
                new MeasurementParams(10.0, 2.0)
        };

        InstructionTable table = InstructionTable.generate(pwd, measurementParams, random).table;

        double[] measurements = new double[] {
                9.9999, // expect alpha
                10.0, // expect beta
                10.0001 // expect beta
        };

        InstructionTable.Entry.Column[] expected = new InstructionTable.Entry.Column[] {
                ALPHA,
                BETA,
                BETA
        };

        Assert.assertArrayEquals(expected, table.selectColumn(measurements, measurementParams));

    }

    @Test
    public void testInterpolateHpwd() throws Exception {
        Password pwd = new Password("asdf");
        MeasurementParams[] measurementParams = randomMeasurementParams.nextMeasurementParams(3);

        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
            InstructionTable.generate(pwd, measurementParams, random);

        // all alpha and beta are good, so any random measurements should interpolate correct hpwd
        double[] measurements = new double[measurementParams.length];
        for (int i = 0; i < measurements.length; i++) {
            measurements[i] = measurementParams[i].responseMean() + random.nextInt(6) - 3;
        }

        HardenedPassword hpwd = tableAndHpwd.table.interpolateHpwd(pwd, measurements, measurementParams);
        Assert.assertEquals(tableAndHpwd.hpwd, hpwd);

        Password wrongPwd = new Password("asdg");
        HardenedPassword wrongHpwd = tableAndHpwd.table.interpolateHpwd(wrongPwd, measurements, measurementParams);
        Assert.assertNotEquals(tableAndHpwd.hpwd, wrongHpwd);
    }
}
