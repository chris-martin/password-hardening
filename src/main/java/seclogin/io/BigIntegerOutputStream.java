package seclogin.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;

/** An output stream that can write {@link BigInteger}s in a format readable by {@link BigIntegerInputStream}. */
public class BigIntegerOutputStream extends DataOutputStream {

    public BigIntegerOutputStream(OutputStream out) {
        super(out);
    }

    /** Write the given {@link BigInteger}. */
    public void writeBigInteger(BigInteger value) throws IOException {
        byte[] bytes = value.toByteArray();
        writeInt(bytes.length);
        write(bytes);
    }
}
