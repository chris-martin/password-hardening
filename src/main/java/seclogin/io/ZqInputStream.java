package seclogin.io;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import com.google.common.io.ByteStreams;

public class ZqInputStream extends FilterInputStream {

    public ZqInputStream(InputStream in) {
        super(in);
    }

    public BigInteger readBigInteger() throws IOException {
        byte[] b = new byte[ZqOutputStream.Q_LEN_IN_BYTES];
        int bytesRead = ByteStreams.read(this, b, 0, b.length);
        if (bytesRead == 0) {
            return null;
        } else if (bytesRead != b.length) {
            throw new IOException("Expected " + b.length + " bytes.");
        }
        return new BigInteger(b);
    }
}