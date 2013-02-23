package seclogin.historyfile;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seclogin.TestRandom;

public class HistoryFileSerializationTest {

    RandomHistoryFile randomHistoryFile;

    @Before
    public void setUp() throws Exception {
        randomHistoryFile = RandomHistoryFile.random(TestRandom.random());
    }

    @Test
    public void testToByteArrayAndFromByteArray() throws Exception {
        HistoryFile historyFile = randomHistoryFile.nextHistoryFile();
        HistoryFileSerialization serialization = new HistoryFileSerialization();
        Assert.assertEquals(historyFile, serialization.fromByteArray(serialization.toByteArray(historyFile)));
    }
}
