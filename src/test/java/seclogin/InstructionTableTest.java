package seclogin;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seclogin.math.PRG;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Random;

public class InstructionTableTest {

    private Random random;

    private MeasurementParams[] measurementParams;

    @Before
    public void setUp() throws Exception {
        random = PRG.random();
        measurementParams = new MeasurementParams[3];
        for (int i = 0; i < measurementParams.length; i++) {
            measurementParams[i] = new MeasurementParams(random.nextInt(20), 1.0);
        }
    }

    @Test
    public void testWriteAndRead() throws Exception {
        InstructionTable written =
            InstructionTable.generate("asdf", measurementParams, null, random).table;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        written.write(out);

        InstructionTable read = InstructionTable.read(new ByteArrayInputStream(out.toByteArray()), measurementParams);

        Assert.assertEquals(written, read);
    }

    @Test
    public void testInterpolateHpwd() throws Exception {
        String pwd = "asdf";
        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
            InstructionTable.generate(pwd, measurementParams, null, random);

        double[] measurements = new double[measurementParams.length];
        for (int i = 0; i < measurements.length; i++) {
            measurements[i] = measurementParams[i].responseMean() + random.nextInt(6) - 3;
        }

        BigInteger hpwd = tableAndHpwd.table.interpolateHpwd(pwd, measurements);
        Assert.assertEquals(tableAndHpwd.hpwd, hpwd);
    }
}
