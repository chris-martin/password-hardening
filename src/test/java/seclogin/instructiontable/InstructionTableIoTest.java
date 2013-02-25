package seclogin.instructiontable;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import seclogin.Password;
import seclogin.TestRandom;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Random;

public class InstructionTableIoTest {

    private Random random;

    @Before
    public void setUp() throws Exception {
        random = TestRandom.random();
    }

    @Test
    public void measurementParams() throws Exception {
        InstructionTable written = InstructionTable.generate(new Password("asdf"), new Distinguishment[3], random).table;

        InstructionTableIo io = new InstructionTableIo();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        io.write(written, out);

        InstructionTable read = io.read(new ByteArrayInputStream(out.toByteArray()));

        Assert.assertEquals(written, read);
    }
}
