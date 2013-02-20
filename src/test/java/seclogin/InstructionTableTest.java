package seclogin;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seclogin.math.PRG;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class InstructionTableTest {

    private Random random;

    private int nrOfFeatures = 3;

    @Before
    public void setUp() throws Exception {
        random = PRG.random();
    }

    @Test
    public void testWriteAndRead() throws Exception {
        InstructionTable written =
            InstructionTable.generate(new Feature[nrOfFeatures], new Password("asdf"), random).table;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        written.write(out);

        InstructionTable read = InstructionTable.read(new ByteArrayInputStream(out.toByteArray()), nrOfFeatures);

        Assert.assertEquals(written, read);
    }

    @Test
    public void testInterpolateHpwd() throws Exception {
        Password pwd = new Password("asdf");
        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
            InstructionTable.generate(new Feature[nrOfFeatures], pwd, random);

        Feature[] features = new Feature[nrOfFeatures];

        Arrays.fill(features, Feature.ALPHA);
        BigInteger hpwd = tableAndHpwd.table.interpolateHpwd(pwd, features);
        Assert.assertEquals(tableAndHpwd.hpwd, hpwd);

        Arrays.fill(features, Feature.BETA);
        hpwd = tableAndHpwd.table.interpolateHpwd(pwd, features);
        Assert.assertEquals(tableAndHpwd.hpwd, hpwd);
    }
}
