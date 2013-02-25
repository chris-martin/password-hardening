package seclogin.instructiontable;

import com.google.common.io.BaseEncoding;
import seclogin.HardenedPassword;
import seclogin.Password;
import seclogin.SecurityParameters;
import seclogin.math.Interpolation;
import seclogin.math.Mod;
import seclogin.math.PasswordBasedPRF;
import seclogin.math.Point;
import seclogin.math.Polynomial;
import seclogin.math.RandomBigIntModQ;
import seclogin.math.RandomPolynomial;
import seclogin.math.SparsePRP;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static seclogin.instructiontable.Distinguishment.ALPHA;
import static seclogin.instructiontable.Distinguishment.BETA;

/**
 * An instruction table that contains values from which the hardened password can be recovered
 * given the correct regular password and measurements similar to those measured during previous
 * successful login attempts for the user to which this table belongs.
 */
public class InstructionTable {

    public static final int R_LEN_IN_BYTES = SecurityParameters.R_LEN / Byte.SIZE;

    final Mod q;
    final byte[] r;
    final Entry[] table;

    InstructionTable(Mod q, byte[] r, Entry[] table) {
        checkArgument(r.length == R_LEN_IN_BYTES);
        this.q = checkNotNull(q);
        this.r = checkNotNull(r);
        this.table = checkNotNull(table);
    }

    /**
     * Interpolates the hardened password using the (x,y) pairs recovered from the table
     * using the given regular password and measurements.
     */
    public HardenedPassword interpolateHpwd(Password pwd, Distinguishment[] distinguishments) {
        List<Point> xys = points(pwd, distinguishments);
        return new HardenedPassword(new Interpolation(xys, q).yIntercept());
    }

    /**
     * Selects the correct (x,y) pair stored in the table for each feature using the given
     * distinguishments and `decrypting` the y value using the given regular password.
     */
    List<Point> points(Password pwd, Distinguishment[] distinguishments) {
        checkState(distinguishments.length == table.length);

        PasswordBasedPRF g = PasswordBasedPRF.forSaltedPassword(r, pwd, q);
        SparsePRP p = new SparsePRP(r, q);

        List<Point> points = new ArrayList<Point>();
        for (int i = 0; i < table.length ; i++) {
            Distinguishment distinguishment = checkNotNull(distinguishments[i]);

            int input = distinguishment == ALPHA ? (2*i) : ((2*i)+1);
            BigInteger x = p.apply(input);
            BigInteger y = table[i].get(distinguishment).subtract(g.of(input)).mod(q.q);

            points.add(new Point(x, y));
        }
        return points;
    }

    /**
     * Generates an instruction table and corresponding hardened password using the
     * given regular password and given feature distinguishments for the user.
     */
    public static InstructionTableAndHardenedPassword generate(Password pwd,
                                                               Distinguishment[] distinguishments,
                                                               Random random) {
        checkNotNull(pwd);
        checkNotNull(distinguishments);
        checkNotNull(random);

        Mod q = new Mod(BigInteger.probablePrime(SecurityParameters.Q_LEN, random));
        RandomBigIntModQ randomBigIntModQ = new RandomBigIntModQ(random, q);

        Polynomial f = new RandomPolynomial(randomBigIntModQ).nextPolynomial(distinguishments.length);

        HardenedPassword hpwd = new HardenedPassword(f.apply(BigInteger.ZERO));

        byte[] r = new byte[R_LEN_IN_BYTES];
        random.nextBytes(r);

        PasswordBasedPRF g = PasswordBasedPRF.forSaltedPassword(r, pwd, q);
        SparsePRP p = new SparsePRP(r, q);

        Entry[] table = new Entry[distinguishments.length];
        for (int i = 0; i < table.length; i++) {
            Distinguishment distinguishment = distinguishments[i];

            BigInteger y0 = f.apply(p.apply(2*i));
            BigInteger alpha = y0.add(g.of(2*i)).mod(q.q);
            if (distinguishment == BETA) {
                alpha = randomBigIntModQ.nextBigIntegerModQNotEqualTo(alpha);
            }

            BigInteger y1 = f.apply(p.apply((2*i)+1));
            BigInteger beta = y1.add(g.of((2*i)+1)).mod(q.q);
            if (distinguishment == ALPHA) {
                beta = randomBigIntModQ.nextBigIntegerModQNotEqualTo(beta);
            }

            table[i] = new Entry(alpha, beta);
        }

        return new InstructionTableAndHardenedPassword(new InstructionTable(q, r, table), hpwd);
    }

    /** An instruction table and its corresponding hardened password. */
    public static final class InstructionTableAndHardenedPassword {
        public final InstructionTable table;
        public final HardenedPassword hpwd;

        public InstructionTableAndHardenedPassword(InstructionTable table, HardenedPassword hpwd) {
            this.table = table;
            this.hpwd = hpwd;
        }
    }

    /** An entry in the instruction table. */
    static class Entry {
        public final BigInteger alpha;
        public final BigInteger beta;

        public Entry(BigInteger alpha, BigInteger beta) {
            this.alpha = alpha;
            this.beta = beta;
        }

        /** Gets the entry value for the given distinguishment */
        BigInteger get(Distinguishment distinguishment) {
            checkNotNull(distinguishment);
            return distinguishment == ALPHA ? alpha : beta;
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

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(String.format("q=%s\n", q.q.toString(16)));
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
        result = 31 * result + Arrays.hashCode(table);
        return result;
    }
}
