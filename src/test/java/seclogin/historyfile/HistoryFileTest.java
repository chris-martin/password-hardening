package seclogin.historyfile;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seclogin.TestRandom;
import seclogin.User;

import java.util.Arrays;
import java.util.Random;

public class HistoryFileTest {

    Random random;

    int maxNrOfEntries = 2;
    int nrOfFeatures = 3;
    final HistoryFileParams params = new HistoryFileParams(maxNrOfEntries, nrOfFeatures);

    @Before
    public void setUp() throws Exception {
        random = TestRandom.random();
    }

    @Test
    public void testEmptyHistoryFile() throws Exception {
        HistoryFile empty = HistoryFile.emptyHistoryFile(new User("asdf"), params);

        Assert.assertEquals(0, empty.nrOfMeasurements);

        double[][] expectedMeasurements = new double[params.maxNrOfMeasurements][params.nrOfFeatures];
        for (double[] expectedMeasurement : expectedMeasurements) {
            Arrays.fill(expectedMeasurement, Double.NaN);
        }
        Assert.assertArrayEquals(expectedMeasurements, empty.measurements);
    }

    @Test
    public void testVerifyUserHash() throws Exception {
        User user = new User("someuser");
        HistoryFile historyFile = HistoryFile.emptyHistoryFile(user, params);
        Assert.assertTrue(historyFile.verifyUserHash(new User("someuser")));
        Assert.assertFalse(historyFile.verifyUserHash(new User("someotheruser")));
    }

    @Test
    public void testWithMostRecentMeasurements() throws Exception {
        // TODO
    }

    @Test
    public void testCalculateStats() throws Exception {
        // TODO
    }
}
