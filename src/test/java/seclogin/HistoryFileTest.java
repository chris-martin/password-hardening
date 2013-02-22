package seclogin;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;

public class HistoryFileTest {

    private SecureRandom random;

    int maxNrOfEntries = 2;
    int nrOfFeatures = 3;
    private final HistoryFileParams params = new HistoryFileParams(maxNrOfEntries, nrOfFeatures);

    @Before
    public void setUp() throws Exception {
        random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(new byte[0]);
    }

    @Test
    public void testAsByteArrayAndFromByteArray() throws Exception {
        HistoryFile historyFile = randomHistoryFile();
        byte[] bytes = historyFile.asByteArray();
        Assert.assertEquals(historyFile, HistoryFile.fromByteArray(bytes, params));
    }

    @Test
    public void testEncryptAndDecrypt() throws Exception {
        BigInteger hpwd = new BigInteger(SecurityParameters.Q_LEN, random);

        HistoryFile original = randomHistoryFile();
        HistoryFile.Encrypted encrypted = original.encrypt(hpwd);
        HistoryFile decrypted = encrypted.decrypt(hpwd, params);

        Assert.assertEquals(original, decrypted);
    }

    @Test(expected = IndecipherableHistoryFileException.class)
    public void testEncryptAndDecryptWithWrongHpwd() throws Exception {
        BigInteger hpwd = new BigInteger(SecurityParameters.Q_LEN, random);

        HistoryFile original = randomHistoryFile();
        HistoryFile.Encrypted encrypted = original.encrypt(hpwd);

        BigInteger wrongHpwd = new BigInteger(SecurityParameters.Q_LEN, random);
        HistoryFile decrypted = encrypted.decrypt(wrongHpwd, params);

        Assert.assertNotEquals(original, decrypted);
    }

    @Test
    public void testWriteAndRead() throws Exception {
        BigInteger hpwd = new BigInteger(SecurityParameters.Q_LEN, random);

        HistoryFile written = randomHistoryFile();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        written.encrypt(hpwd).write(out);
        HistoryFile read = HistoryFile.read(new ByteArrayInputStream(out.toByteArray())).decrypt(hpwd, params);

        Assert.assertEquals(written, read);
    }

    private HistoryFile randomHistoryFile() {
        HistoryFile written = HistoryFile.emptyHistoryFile("asdf", params);
        for (int i = 0; i < params.maxNrOfEntries(); ++i) {
            written = written.withMostRecentMeasurements(randomMeasurements());
        }
        return written;
    }

    private double[] randomMeasurements() {
        double[] measurements = new double[params.nrOfFeatures()];
        for (int i = 0; i < measurements.length; i++) {
            measurements[i] = randomMeasurement();
        }
        return measurements;
    }

    private double randomMeasurement() {
        return random.nextInt(20);
    }
}
