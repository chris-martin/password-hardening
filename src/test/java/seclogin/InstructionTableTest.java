package seclogin;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seclogin.math.PRG;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
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
            InstructionTable.generate(allFeatureValues(null), new Password("asdf"), random).table;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        written.write(out);

        InstructionTable read = InstructionTable.read(new ByteArrayInputStream(out.toByteArray()), nrOfFeatures);

        Assert.assertEquals(written, read);
    }

    private List<FeatureValue> allFeatureValues(FeatureValue value) {
        return Collections.nCopies(nrOfFeatures, value);
    }

    @Test
    public void testInterpolateHpwd() throws Exception {
        Password pwd = new Password("asdf");
        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
            InstructionTable.generate(allFeatureValues(null), pwd, random);

        BigInteger hpwd = tableAndHpwd.table.interpolateHpwd(pwd, allFeatureValues(FeatureValue.ALPHA));
        Assert.assertEquals(tableAndHpwd.hpwd, hpwd);

        hpwd = tableAndHpwd.table.interpolateHpwd(pwd, allFeatureValues(FeatureValue.BETA));
        Assert.assertEquals(tableAndHpwd.hpwd, hpwd);
    }
}
