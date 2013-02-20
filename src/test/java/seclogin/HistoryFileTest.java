package seclogin;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seclogin.math.PRG;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.Random;

public class HistoryFileTest {
    private Random random;

    @Before
    public void setUp() throws Exception {
        random = PRG.random();
    }

    @Test
    public void testEncryptAndDecrypt() throws Exception {
        BigInteger hpwd = new BigInteger(Parameters.Q_LEN, random);

        HistoryFile original = randomHistoryFile();
        HistoryFile.Encrypted encrypted = original.encrypt(hpwd);
        HistoryFile decrypted = encrypted.decrypt(hpwd);

        Assert.assertEquals(original, decrypted);
    }

    @Test(expected = IndecipherableHistoryFileException.class)
    public void testEncryptAndDecryptWithWrongHpwd() throws Exception {
        BigInteger hpwd = new BigInteger(Parameters.Q_LEN, random);

        HistoryFile original = randomHistoryFile();
        HistoryFile.Encrypted encrypted = original.encrypt(hpwd);

        BigInteger wrongHpwd = new BigInteger(Parameters.Q_LEN, random);
        HistoryFile decrypted = encrypted.decrypt(wrongHpwd);

        Assert.assertNotEquals(original, decrypted);
    }

    @Test
    public void testWriteAndRead() throws Exception {
        BigInteger hpwd = new BigInteger(Parameters.Q_LEN, random);

        HistoryFile written = randomHistoryFile();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        written.encrypt(hpwd).write(out);
        HistoryFile read = HistoryFile.read(new ByteArrayInputStream(out.toByteArray())).decrypt(hpwd);

        Assert.assertEquals(written, read);
    }

    private HistoryFile randomHistoryFile() {
        HistoryFile written = HistoryFile.emptyHistoryFile("asdf");
        for (int i = 0; i < Parameters.H; ++i) {
            written = written.withMostRecentMeasurements(randomMeasurements());
        }
        return written;
    }

    private double[] randomMeasurements() {
        double[] measurements = new double[Parameters.M];
        for (int i = 0; i < measurements.length; i++) {
            measurements[i] = randomMeasurement();
        }
        return measurements;
    }

    private double randomMeasurement() {
        return random.nextInt(20);
    }
}
