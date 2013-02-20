package seclogin;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Random;

public class InstructionTableTest {

    private Random random;

    @Before
    public void setUp() throws Exception {
        random = new Random(0L);
    }

    @Test
    public void testWriteAndRead() throws Exception {
        InstructionTable written = InstructionTable.generate(new Password("asdf".toCharArray()), random).table;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        written.write(out);

        InstructionTable read = InstructionTable.read(new ByteArrayInputStream(out.toByteArray()));

        Assert.assertEquals(written, read);
    }

    @Test
    public void testInterpolateHpwd() throws Exception {
        Password pwd = new Password("asdf".toCharArray());
        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd = InstructionTable.generate(pwd, random);

        Feature[] features = new Feature[Parameters.M];

        Arrays.fill(features, Feature.ALPHA);
        BigInteger hpwd = tableAndHpwd.table.interpolateHpwd(pwd, features);
        Assert.assertEquals(tableAndHpwd.hpwd, hpwd);

        Arrays.fill(features, Feature.BETA);
        hpwd = tableAndHpwd.table.interpolateHpwd(pwd, features);
        Assert.assertEquals(tableAndHpwd.hpwd, hpwd);
    }
}
