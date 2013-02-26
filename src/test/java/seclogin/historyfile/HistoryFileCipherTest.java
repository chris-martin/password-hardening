package seclogin.historyfile;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seclogin.HardenedPassword;
import seclogin.TestRandom;
import seclogin.User;
import seclogin.math.Mod;
import seclogin.math.RandomBigIntModQ;
import seclogin.math.RandomQ;

import java.util.Random;

public class HistoryFileCipherTest {

    Random random;
    RandomHistoryFile randomHistoryFile;
    Mod q;

    @Before
    public void setUp() throws Exception {
        random = TestRandom.random();
        randomHistoryFile = RandomHistoryFile.random(random);
        q = new RandomQ(random).nextQ();
    }

    @Test
    public void testEncryptAndDecrypt() throws Exception {
        HardenedPassword hpwd = randomHardenedPassword();

        User user = new User("someuser");
        HistoryFile original = randomHistoryFile.nextHistoryFile(user);
        
        HistoryFileCipher cipher = new HistoryFileCipher();
        EncryptedHistoryFile encrypted = cipher.encrypt(original, hpwd);
        HistoryFile decrypted = cipher.decrypt(encrypted, hpwd, user);

        Assert.assertEquals(original, decrypted);
    }
    
    private HardenedPassword randomHardenedPassword() {
        return new HardenedPassword(new RandomBigIntModQ(random, q).nextBigIntModQ());
    }

    @Test(expected = IndecipherableHistoryFileException.class)
    public void testEncryptAndDecryptWithWrongHpwd() throws Exception {
        HardenedPassword hpwd = randomHardenedPassword();

        User user = new User("someuser");
        HistoryFile original = randomHistoryFile.nextHistoryFile(user);

        HistoryFileCipher cipher = new HistoryFileCipher();
        EncryptedHistoryFile encrypted = cipher.encrypt(original, hpwd);

        HardenedPassword wrongHpwd = randomHardenedPassword();
        Assert.assertNotEquals(hpwd, wrongHpwd);

        cipher.decrypt(encrypted, wrongHpwd, user);
    }

    @Test(expected = IndecipherableHistoryFileException.class)
    public void testEncryptAndDecryptWithWrongUser() throws Exception {
        HardenedPassword hpwd = randomHardenedPassword();

        User user = new User("someuser");
        HistoryFile original = randomHistoryFile.nextHistoryFile(user);

        HistoryFileCipher cipher = new HistoryFileCipher();
        EncryptedHistoryFile encrypted = cipher.encrypt(original, hpwd);

        User wrongUser = new User("someotheruser");
        Assert.assertNotEquals(user, wrongUser);

        cipher.decrypt(encrypted, hpwd, wrongUser);
    }
}
