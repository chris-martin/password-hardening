package seclogin.io;

import com.google.common.math.IntMath;
import seclogin.SecurityParameters;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.math.RoundingMode;

import static com.google.common.base.Preconditions.checkArgument;

/** An output stream that can write {@link BigInteger}s in Z_q using a fixed number of bytes. */
public class ZqOutputStream extends FilterOutputStream {

    static final int Q_LEN_IN_BYTES = IntMath.divide(SecurityParameters.Q_LEN + 1, Byte.SIZE, RoundingMode.CEILING); // account for sign bit

    public ZqOutputStream(OutputStream out) {
        super(out);
    }

    /** Write the given {@link BigInteger} using a fixed number of bytes. */
    public void writeBigInteger(BigInteger value) throws IOException {
        checkArgument(value.signum() != -1);
        checkArgument(value.bitLength() <= SecurityParameters.Q_LEN);
        byte[] b = value.toByteArray();
        for (int c = b.length; c < Q_LEN_IN_BYTES; ++c) {
            write(0);
        }
        write(b);
    }
}
