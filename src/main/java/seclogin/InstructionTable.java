package seclogin;

import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import scala.math.BigInt;
import seclogin.io.ZqInputStream;
import seclogin.io.ZqOutputStream;
import seclogin.math.Interpolation;
import seclogin.math.Mod;
import seclogin.math.PasswordBasedPRF;
import seclogin.math.Polynomial;
import seclogin.math.RandomPolynomial;
import seclogin.math.SparsePRP;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An instruction table that contains values from which the hardened password can be recovered
 * given the correct regular password and measurements similar to those measured during previous
 * successful login attempts for the user to which this table belongs.
 */
public class InstructionTable {

    public static final int R_LEN_IN_BYTES = SecurityParameters.R_LEN / Byte.SIZE;

    private final BigInteger q;
    private final byte[] r;
    private final Entry[] table;

    private final MeasurementParams[] measurementParams;

    private InstructionTable(BigInteger q, byte[] r, Entry[] table, MeasurementParams[] measurementParams) {
        checkArgument(r.length == R_LEN_IN_BYTES);
        this.q = checkNotNull(q);
        this.r = checkNotNull(r);
        this.table = checkNotNull(table);
        this.measurementParams = checkNotNull(measurementParams);
    }

    /**
     * Interpolates the hardened password using the (x,y) pairs recovered from the table
     * using the given regular password and measurements.
     */
    public BigInteger interpolateHpwd(String pwd, double[] measurements) {
        List<BigInteger> xys = xys(pwd, measurements);
        return new Interpolation(xys, q).yIntercept().bigInteger();
    }

    /**
     * Selects the correct (x,y) pair stored in the table for each feature using the given
     * measurements and `decrypting` the y value using the given regular password.
     */
    List<BigInteger> xys(String pwd, double[] measurements) {
        checkArgument(measurements.length == table.length);

        PasswordBasedPRF g = PasswordBasedPRF.forSaltedPassword(r, pwd, q);
        SparsePRP p = new SparsePRP(r, q);

        List<BigInteger> xys = Lists.newArrayListWithCapacity(measurements.length * 2);
        for (int i = 0; i < measurements.length ; i++) {
            Entry entry = table[i];

            BigInteger x, y;
            if (measurements[i] < measurementParams[i].responseMean()) {
                x = p.apply(2*i).bigInteger();
                y = entry.alpha.subtract(g.of(2*i)).mod(q);
            } else {
                x = p.apply((2*i)+1).bigInteger();
                y = entry.beta.subtract(g.of((2*i)+1)).mod(q);
            }

            xys.add(x);
            xys.add(y);
        }
        return xys;
    }

    /**
     * Generates the an instruction table and hardened password using the given
     * regular password and, if supplied, measurement statistics particular to
     * the user. If no measurement stats are given, the user is not yet
     * distinguished by any features.
     */
    public static InstructionTableAndHardenedPassword generate(String pwd,
                                                               MeasurementParams[] measurementParams,
                                                               @Nullable MeasurementStats[] measurementStats,
                                                               Random random) {
        checkNotNull(measurementParams);

        BigInteger q = BigInteger.probablePrime(SecurityParameters.Q_LEN, random);
        Mod zq = new Mod(q);
        Polynomial f = new RandomPolynomial(random).nextPolynomial(measurementParams.length, q);

        BigInteger hpwd = f.apply(BigInteger.ZERO);

        byte[] r = new byte[R_LEN_IN_BYTES];
        random.nextBytes(r);

        PasswordBasedPRF g = PasswordBasedPRF.forSaltedPassword(r, pwd, q);
        SparsePRP p = new SparsePRP(r, q);

        Entry[] table = new Entry[measurementParams.length];
        for (int i = 0; i < table.length; i++) {
            // calculate `good' values for both alpha and beta
            BigInteger y0 = f.apply(p.apply(2*i)).bigInteger();
            BigInteger alpha = y0.add(g.of(2*i)).mod(q);

            BigInteger y1 = f.apply(p.apply((2*i)+1)).bigInteger();
            BigInteger beta = y1.add(g.of((2*i)+1)).mod(q);

            if (measurementStats != null) {
                MeasurementParams system = measurementParams[i];
                MeasurementStats user = measurementStats[i];
                checkNotNull(user);

                // if this feature is distinguishing for this user, make only one of alpha or beta `good'
                if (Math.abs(user.mean() - system.responseMean()) > (user.stDev() * system.stDevMultiplier())) {
                    if (user.mean() < system.responseMean()) {
                        beta = zq.randomElementNotEqualTo(BigInt.apply(beta), random).bigInteger();
                    } else {
                        alpha = zq.randomElementNotEqualTo(BigInt.apply(alpha), random).bigInteger();
                    }
                }
            }

            table[i] = new Entry(alpha, beta);
        }

        return new InstructionTableAndHardenedPassword(new InstructionTable(q, r, table, measurementParams), hpwd);
    }

    /** An instruction table and its corresponding hardened password. */
    public static final class InstructionTableAndHardenedPassword {
        public final InstructionTable table;
        public final BigInteger hpwd;

        public InstructionTableAndHardenedPassword(InstructionTable table, BigInteger hpwd) {
            this.table = table;
            this.hpwd = hpwd;
        }
    }

    /** An entry in the instruction table. */
    private static class Entry {
        public final BigInteger alpha;
        public final BigInteger beta;

        public Entry(BigInteger alpha, BigInteger beta) {
            this.alpha = alpha;
            this.beta = beta;
        }

        private void write(ZqOutputStream out) throws IOException {
            out.writeBigInteger(alpha);
            out.writeBigInteger(beta);
        }

        private static Entry read(ZqInputStream in) throws IOException {
            BigInteger alpha = in.readBigInteger();
            if (alpha == null) {
                return null;
            }
            BigInteger beta = in.readBigInteger();
            return new Entry(alpha, beta);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry that = (Entry) o;
            if (!alpha.equals(that.alpha)) return false;
            if (!beta.equals(that.beta)) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int result = alpha.hashCode();
            result = 31 * result + beta.hashCode();
            return result;
        }
    }

    /** Writes this instruction table to the given stream. */
    public void write(OutputStream outputStream) throws IOException {
        ZqOutputStream out = new ZqOutputStream(new BufferedOutputStream(outputStream));
        out.writeBigInteger(q);
        out.write(r);
        for (Entry entry : table) {
            entry.write(out);
        }
        out.flush();
    }

    /** Reads the instruction table supplied by the given stream. */
    public static InstructionTable read(InputStream inputStream, MeasurementParams[] measurementParams) throws IOException {
        ZqInputStream in = new ZqInputStream(new BufferedInputStream(inputStream));
        try {
            BigInteger q = in.readBigInteger();
            byte[] r = new byte[R_LEN_IN_BYTES];
            in.read(r);

            Entry[] entries = new Entry[measurementParams.length];
            for (int i = 0; ; i++) {
                Entry entry = Entry.read(in);
                if (entry == null) {
                    break;
                }
                entries[i] = entry;
            }
            return new InstructionTable(q, r, entries, measurementParams);
        } finally {
            in.close();
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(String.format("q=%s\n", q.toString(16)));
        s.append(String.format("r=%s\n", BaseEncoding.base16().lowerCase().encode(r)));
        for (int i = 0; i < table.length; i++) {
            Entry entry = table[i];
            s.append(String.format("a_%d=%s   b_%d=%s\n",
                    i, entry.alpha.toString(16),
                    i, entry.beta.toString(16)));
        }
        return s.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InstructionTable that = (InstructionTable) o;
        if (!q.equals(that.q)) return false;
        if (!Arrays.equals(r, that.r)) return false;
        if (!Arrays.equals(table, that.table)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = q.hashCode();
        result = 31 * result + Arrays.hashCode(r);
        result = 31 * result + table.hashCode();
        return result;
    }
}
