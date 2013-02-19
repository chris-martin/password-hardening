package seclogin;

import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * A password. Consumers of this password should call {@link #destroy()} as
 * soon as the underlying characters are no longer needed.
 */
public final class Password {

    private final char[] password;

    Password(char[] password) {
        this.password = password;
    }

    public char[] getPassword() {
        return password;
    }

    public byte[] asBytes() {
        return Charset.forName("UTF-8").encode(CharBuffer.wrap(password)).array();
    }

    public void destroy() {
        Arrays.fill(password, '\0');
    }

    @Override
    protected void finalize() throws Throwable {
        destroy();
        super.finalize();
    }
}
