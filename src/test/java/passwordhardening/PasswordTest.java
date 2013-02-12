package passwordhardening;

import org.junit.Assert;
import org.junit.Test;

public class PasswordTest {
    @Test
    public void testDestroy() throws Exception {
        char[] passwordBuffer = {'a', 'b', 'c', 'd'};

        char[] originalPasswordBufferCopy = new char[passwordBuffer.length];
        System.arraycopy(passwordBuffer, 0, originalPasswordBufferCopy, 0, passwordBuffer.length);

        new Password(passwordBuffer).destroy();

        for (int i = 0; i < passwordBuffer.length; i++) {
            Assert.assertNotEquals(passwordBuffer[i], originalPasswordBufferCopy[i]);
        }
    }
}
