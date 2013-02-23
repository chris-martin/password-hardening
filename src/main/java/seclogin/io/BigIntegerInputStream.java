package seclogin.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

/** An input stream that can read {@link BigInteger}s written by {@link BigIntegerOutputStream}. */
public class BigIntegerInputStream extends DataInputStream {

    public BigIntegerInputStream(InputStream in) {
        super(in);
    }

    public BigInteger readBigInteger() throws IOException {
        int len = readInt();
        byte[] bytes = new byte[len];
        readFully(bytes);
        return new BigInteger(bytes);
    }
}
