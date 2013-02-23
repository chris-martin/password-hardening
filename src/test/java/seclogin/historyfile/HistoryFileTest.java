package seclogin.historyfile;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seclogin.TestRandom;
import seclogin.User;

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
        Assert.assertArrayEquals(new double[params.maxNrOfMeasurements][params.nrOfFeatures], empty.measurements);
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
