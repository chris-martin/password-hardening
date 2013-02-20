package seclogin;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

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
        written.encrypt(hpwd).write(out);

        HistoryFile read = HistoryFile.read(new ByteArrayInputStream(out.toByteArray())).decrypt(hpwd);

        Assert.assertEquals(written, read);
    }

    @Test(expected = IndecipherableHistoryFileException.class)
    public void testWriteAndReadWithWrongHpwd() throws Exception {
        BigInteger hpwd = new BigInteger(Parameters.Q_LEN, random);

        HistoryFile written = randomHistoryFile();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        written.encrypt(hpwd).write(out);

        BigInteger wrongHpwd = new BigInteger(Parameters.Q_LEN, random);
        HistoryFile readWithWrongHpwd = HistoryFile.read(new ByteArrayInputStream(out.toByteArray())).decrypt(wrongHpwd);

        Assert.assertNotEquals(written, readWithWrongHpwd);
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
