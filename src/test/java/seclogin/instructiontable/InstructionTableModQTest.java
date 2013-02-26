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

import java.util.Random;

import static seclogin.instructiontable.Distinguishment.ALPHA;
import static seclogin.instructiontable.Distinguishment.BETA;

public class InstructionTableModQTest {

    Random random;
    Mod q;
    InstructionTableModQ instructionTableModQ;

    @Before
    public void setUp() throws Exception {
        random = TestRandom.random();
        q = new RandomQ(random).nextQ();
        instructionTableModQ = new InstructionTableModQ(q, random);
    }

    @Test
    public void testGenerate() throws Exception {
        HardenedPassword hpwd = new HardenedPassword(new RandomBigIntModQ(random, q).nextBigIntModQ());
        Password pwd = new Password("asdf");
        InstructionTable table = instructionTableModQ.generate(hpwd, pwd, new Distinguishment[3]);

        Assert.assertEquals(3, table.table.length);
    }

    @Test
    public void testInterpolateHpwd() throws Exception {
        HardenedPassword hpwd = new HardenedPassword(new RandomBigIntModQ(random, q).nextBigIntModQ());
        Password pwd = new Password("asdf");
        InstructionTable table = instructionTableModQ.generate(hpwd, pwd, new Distinguishment[]{ ALPHA, null, BETA });

        // right password, right distinguishments
        HardenedPassword rightHpwd = instructionTableModQ.interpolateHpwd(table, pwd, new Distinguishment[]{ ALPHA, ALPHA, BETA });
        Assert.assertEquals(hpwd, rightHpwd);
        rightHpwd = instructionTableModQ.interpolateHpwd(table, pwd, new Distinguishment[]{ ALPHA, BETA, BETA });
        Assert.assertEquals(hpwd, rightHpwd);

        // wrong password
        Password wrongPwd = new Password("asdg");
        HardenedPassword wrongHpwd = instructionTableModQ.interpolateHpwd(table, wrongPwd, new Distinguishment[]{ ALPHA, ALPHA, BETA });
        Assert.assertNotEquals(hpwd, wrongHpwd);
        wrongHpwd = instructionTableModQ.interpolateHpwd(table, wrongPwd, new Distinguishment[]{ ALPHA, BETA, BETA });
        Assert.assertNotEquals(hpwd, wrongHpwd);

        // wrong distinguishments
        wrongHpwd = instructionTableModQ.interpolateHpwd(table, pwd, new Distinguishment[]{ BETA, ALPHA, BETA });
        Assert.assertNotEquals(hpwd, wrongHpwd);
        wrongHpwd = instructionTableModQ.interpolateHpwd(table, pwd, new Distinguishment[]{ ALPHA, ALPHA, ALPHA });
        Assert.assertNotEquals(hpwd, wrongHpwd);
        wrongHpwd = instructionTableModQ.interpolateHpwd(table, pwd, new Distinguishment[]{ BETA, ALPHA, ALPHA });
        Assert.assertNotEquals(hpwd, wrongHpwd);
        wrongHpwd = instructionTableModQ.interpolateHpwd(table, pwd, new Distinguishment[]{ BETA, BETA, BETA });
        Assert.assertNotEquals(hpwd, wrongHpwd);
        wrongHpwd = instructionTableModQ.interpolateHpwd(table, pwd, new Distinguishment[]{ ALPHA, BETA, ALPHA });
        Assert.assertNotEquals(hpwd, wrongHpwd);
        wrongHpwd = instructionTableModQ.interpolateHpwd(table, pwd, new Distinguishment[]{ BETA, BETA, ALPHA });
        Assert.assertNotEquals(hpwd, wrongHpwd);

        // wrong password, wrong distinguishments
        wrongHpwd = instructionTableModQ.interpolateHpwd(table, wrongPwd, new Distinguishment[]{ BETA, BETA, BETA });
        Assert.assertNotEquals(hpwd, wrongHpwd);
    }
}
