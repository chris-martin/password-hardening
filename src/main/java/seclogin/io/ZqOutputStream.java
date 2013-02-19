package seclogin.io;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.math.RoundingMode;

import com.google.common.math.IntMath;
import seclogin.Parameters;

import static com.google.common.base.Preconditions.checkArgument;

public class ZqOutputStream extends FilterOutputStream {

    static final int Q_LEN_IN_BYTES = IntMath.divide(Parameters.Q_LEN + 1, Byte.SIZE, RoundingMode.CEILING); // account for sign bit

    public ZqOutputStream(OutputStream out) {
        super(out);
    }

    public void writeBigInteger(BigInteger value) throws IOException {
        checkArgument(value.signum() != -1);
        checkArgument(value.bitLength() <= Parameters.Q_LEN);
        byte[] b = value.toByteArray();
        for (int c = b.length; c < Q_LEN_IN_BYTES; ++c) {
            write(0);
        }
        write(b);
    }
}
