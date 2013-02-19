package seclogin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.util.Random;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class InstructionTableTest {

    private Random random;

    @Before
    public void setUp() throws Exception {
        random = new SecureRandom(new byte[1]);
    }

    @Test
    public void testWriteAndRead() throws Exception {
        InstructionTable written = InstructionTable.generate(
                Parameters.M,
                new Password("asdf".toCharArray()),
                random).table;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        written.write(out);

        InstructionTable read = InstructionTable.read(new ByteArrayInputStream(out.toByteArray()));

        Assert.assertEquals(written, read);
    }
}
