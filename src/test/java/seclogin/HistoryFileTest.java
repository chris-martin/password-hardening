package seclogin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class HistoryFileTest {
    private Random random;

    @Before
    public void setUp() throws Exception {
        random = new SecureRandom(new byte[1]);
    }

    @Test
    public void testWriteAndRead() throws Exception {
        BigInteger hpwd = new BigInteger(Parameters.Q_LEN, random);

        HistoryFile written = randomHistoryFile();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        written.write(out, hpwd);

        HistoryFile read = HistoryFile.read(new ByteArrayInputStream(out.toByteArray())).decrypt(hpwd);

        Assert.assertEquals(written, read);
    }

    @Test(expected = IndecipherableHistoryFileException.class)
    public void testWriteAndReadWithWrongHpwd() throws Exception {
        BigInteger hpwd = new BigInteger(Parameters.Q_LEN, random);

        HistoryFile written = randomHistoryFile();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        written.write(out, hpwd);

        BigInteger wrongHpwd = new BigInteger(Parameters.Q_LEN, random);
        HistoryFile readWithWrongHpwd = HistoryFile.read(new ByteArrayInputStream(out.toByteArray())).decrypt(wrongHpwd);

        Assert.assertNotEquals(written, readWithWrongHpwd);
    }

    private HistoryFile randomHistoryFile() {
        HistoryFile written = HistoryFile.emptyHistoryFile("asdf");
        for (int i = 0; i < Parameters.H; ++i) {
            written = written.withMostRecentFeatures(randomFeatures());
        }
        return written;
    }

    private Feature[] randomFeatures() {
        Feature[] features = new Feature[Parameters.M];
        for (int i = 0; i < features.length; i++) {
            features[i] = randomFeature();
        }
        return features;
    }

    private Feature randomFeature() {
        int i = random.nextInt(Feature.values().length + 1);
        return i == 0 ? null : Feature.values()[i - 1];
    }
}
