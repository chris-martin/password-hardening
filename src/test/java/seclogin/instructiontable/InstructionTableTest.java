package seclogin.instructiontable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seclogin.HardenedPassword;
import seclogin.MeasurementParams;
import seclogin.Password;
import seclogin.TestRandom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

public class InstructionTableTest {

    private Random random;

    private MeasurementParams[] measurementParams;

    @Before
    public void setUp() throws Exception {
        random = TestRandom.random();
        measurementParams = new MeasurementParams[3];
        for (int i = 0; i < measurementParams.length; i++) {
            measurementParams[i] = new MeasurementParams(random.nextInt(20), 1.0);
        }
    }

    @Test
    public void testWriteAndRead() throws Exception {
        InstructionTable written =
            InstructionTable.generate(new Password("asdf"), measurementParams, null, random).table;

        InstructionTableIo io = new InstructionTableIo();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        io.write(written, out);

        InstructionTable read = io.read(new ByteArrayInputStream(out.toByteArray()));

        Assert.assertEquals(written, read);
    }

    @Test
    public void testInterpolateHpwd() throws Exception {
        Password pwd = new Password("asdf");
        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
            InstructionTable.generate(pwd, measurementParams, null, random);

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
