package seclogin.historyfile;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seclogin.TestRandom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

public class HistoryFileIoTest {

    Random random;

    @Before
    public void setUp() throws Exception {
        random = TestRandom.random();
    }

    @Test
    public void testWriteAndRead() throws Exception {
        byte[] ciphertext = new byte[1024];
        random.nextBytes(ciphertext);

        EncryptedHistoryFile written = new EncryptedHistoryFile(ciphertext);

        HistoryFileIo historyFileIo = new HistoryFileIo();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        historyFileIo.write(written, out);
        EncryptedHistoryFile read = historyFileIo.read(new ByteArrayInputStream(out.toByteArray()));

        Assert.assertEquals(written, read);

    }
}
