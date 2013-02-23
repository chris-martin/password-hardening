package seclogin.historyfile;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seclogin.HardenedPassword;
import seclogin.SecurityParameters;
import seclogin.TestRandom;
import seclogin.User;

import java.math.BigInteger;
import java.util.Random;

public class HistoryFileCipherTest {

    Random random;
    RandomHistoryFile randomHistoryFile;

    @Before
    public void setUp() throws Exception {
        random = TestRandom.random();
        randomHistoryFile = RandomHistoryFile.random(random);
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
        return new HardenedPassword(new BigInteger(SecurityParameters.Q_LEN, random));
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
