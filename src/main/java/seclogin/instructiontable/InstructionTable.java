package seclogin.instructiontable;

import com.google.common.io.BaseEncoding;
import seclogin.Password;
import seclogin.SecurityParameters;
import seclogin.math.Mod;
import seclogin.math.PasswordBasedPRF;
import seclogin.math.Point;
import seclogin.math.SparsePRP;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static seclogin.instructiontable.Distinguishment.ALPHA;

/**
 * An instruction table that contains values from which the hardened password can be recovered
 * given the correct regular password and measurements similar to those measured during previous
 * successful login attempts for the user to which this table belongs.
 */
public class InstructionTable {

    final byte[] r;
    final Entry[] table;

    InstructionTable(byte[] r, Entry[] table) {
        checkArgument(r.length == SecurityParameters.R_LEN/Byte.SIZE);
        this.r = checkNotNull(r);
        this.table = checkNotNull(table);
    }

    /**
     * Selects the correct (x,y) pair stored in the table for each feature using the given
     * distinguishments and `decrypting` the y value using the given regular password.
     */
    List<Point> points(Mod q, Password pwd, Distinguishment[] distinguishments) {
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

    /** An entry in the instruction table. */
    static class Entry {
        public final BigInteger alpha;
        public final BigInteger beta;

        Entry(BigInteger alpha, BigInteger beta) {
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
        if (!Arrays.equals(r, that.r)) return false;
        if (!Arrays.equals(table, that.table)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(r);
        result = 31 * result + Arrays.hashCode(table);
        return result;
    }
}
