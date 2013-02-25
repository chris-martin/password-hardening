package seclogin.instructiontable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seclogin.HardenedPassword;
import seclogin.Password;
import seclogin.TestRandom;

import java.util.Random;

import static seclogin.instructiontable.Distinguishment.ALPHA;
import static seclogin.instructiontable.Distinguishment.BETA;

public class InstructionTableTest {

    private Random random;

    @Before
    public void setUp() throws Exception {
        random = TestRandom.random();
    }

    @Test
    public void testGenerate() throws Exception {
        Password pwd = new Password("asdf");
        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
                InstructionTable.generate(pwd, new Distinguishment[3], random);

        Assert.assertEquals(3, tableAndHpwd.table.table.length);
    }

    @Test
    public void testInterpolateHpwd() throws Exception {
        Password pwd = new Password("asdf");

        Distinguishment[] distinguishments = new Distinguishment[]{ ALPHA, null, BETA };

        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
            InstructionTable.generate(pwd, distinguishments, random);

        // right password, right distinguishments
        HardenedPassword rightHpwd = tableAndHpwd.table.interpolateHpwd(pwd, new Distinguishment[]{ ALPHA, ALPHA, BETA });
        Assert.assertEquals(tableAndHpwd.hpwd, rightHpwd);
        rightHpwd = tableAndHpwd.table.interpolateHpwd(pwd, new Distinguishment[]{ ALPHA, BETA, BETA });
        Assert.assertEquals(tableAndHpwd.hpwd, rightHpwd);

        // wrong password
        Password wrongPwd = new Password("asdg");
        HardenedPassword wrongHpwd = tableAndHpwd.table.interpolateHpwd(wrongPwd, new Distinguishment[]{ ALPHA, ALPHA, BETA });
        Assert.assertNotEquals(tableAndHpwd.hpwd, wrongHpwd);
        wrongHpwd = tableAndHpwd.table.interpolateHpwd(wrongPwd, new Distinguishment[]{ ALPHA, BETA, BETA });
        Assert.assertNotEquals(tableAndHpwd.hpwd, wrongHpwd);

        // wrong distinguishments
        wrongHpwd = tableAndHpwd.table.interpolateHpwd(pwd, new Distinguishment[]{ BETA, ALPHA, BETA });
        Assert.assertNotEquals(tableAndHpwd.hpwd, wrongHpwd);
        wrongHpwd = tableAndHpwd.table.interpolateHpwd(pwd, new Distinguishment[]{ ALPHA, ALPHA, ALPHA });
        Assert.assertNotEquals(tableAndHpwd.hpwd, wrongHpwd);
        wrongHpwd = tableAndHpwd.table.interpolateHpwd(pwd, new Distinguishment[]{ BETA, ALPHA, ALPHA });
        Assert.assertNotEquals(tableAndHpwd.hpwd, wrongHpwd);
        wrongHpwd = tableAndHpwd.table.interpolateHpwd(pwd, new Distinguishment[]{ BETA, BETA, BETA });
        Assert.assertNotEquals(tableAndHpwd.hpwd, wrongHpwd);
        wrongHpwd = tableAndHpwd.table.interpolateHpwd(pwd, new Distinguishment[]{ ALPHA, BETA, ALPHA });
        Assert.assertNotEquals(tableAndHpwd.hpwd, wrongHpwd);
        wrongHpwd = tableAndHpwd.table.interpolateHpwd(pwd, new Distinguishment[]{ BETA, BETA, ALPHA });
        Assert.assertNotEquals(tableAndHpwd.hpwd, wrongHpwd);

        // wrong password, wrong distinguishments
        wrongHpwd = tableAndHpwd.table.interpolateHpwd(wrongPwd, new Distinguishment[]{ BETA, BETA, BETA });
        Assert.assertNotEquals(tableAndHpwd.hpwd, wrongHpwd);
    }
}
