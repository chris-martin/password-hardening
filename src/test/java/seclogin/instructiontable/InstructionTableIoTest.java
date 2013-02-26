package seclogin.instructiontable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seclogin.HardenedPassword;
import seclogin.Password;
import seclogin.TestRandom;
import seclogin.math.Mod;
import seclogin.math.RandomBigIntModQ;
import seclogin.math.RandomQ;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

public class InstructionTableIoTest {

    Random random;
    Mod q;

    @Before
    public void setUp() throws Exception {
        random = TestRandom.random();
        q = new RandomQ(random).nextQ();
    }

    @Test
    public void measurementParams() throws Exception {

        HardenedPassword hpwd = new HardenedPassword(new RandomBigIntModQ(random, q).nextBigIntModQ());
        InstructionTable written = new InstructionTableModQ(q, random).generate(hpwd, new Password("asdf"), new Distinguishment[3]);

        InstructionTableIo io = new InstructionTableIo();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        io.write(written, out);

        InstructionTable read = io.read(new ByteArrayInputStream(out.toByteArray()));

        Assert.assertEquals(written, read);
    }
}
