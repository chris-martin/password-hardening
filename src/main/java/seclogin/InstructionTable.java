package seclogin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import seclogin.io.ZqInputStream;
import seclogin.io.ZqOutputStream;
import seclogin.math.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;
import static seclogin.Feature.ALPHA;
import static seclogin.Feature.BETA;

public class InstructionTable {

    public static final int R_LEN_IN_BYTES = Parameters.R_LEN / Byte.SIZE;

    private final Zq zq;
    private final byte[] r;
    private final Entry[] table;

    private InstructionTable(Zq zq, byte[] r, Entry[] table) {
        checkArgument(r.length == R_LEN_IN_BYTES);
        this.zq = zq;
        this.r = r;
        this.table = table;
    }

    public BigInteger interpolateHpwd(Password pwd, Feature[] features) {
        List<BigInteger> xys = xys(pwd, features);
        return new Interpolation(xys, zq.q).yIntercept().bigInteger();
    }

    private List<BigInteger> xys(Password pwd, Feature[] features) {
        checkArgument(features.length == table.length);

        G g = G.forSaltedPassword(r, pwd, zq);
        P p = new P(r, zq);

        List<BigInteger> xys = Lists.newArrayListWithCapacity(features.length * 2);
        for (int i = 0; i < features.length; i++) {
            Feature feature = features[i];
            checkNotNull(feature);

            Entry entry = table[i];

            BigInteger indexedInput = BigInteger.valueOf(feature == ALPHA ? (2*i) : ((2*i)+1));
            BigInteger x = p.of(indexedInput);
            BigInteger y = (feature == ALPHA ? entry.alpha : entry.beta).subtract(g.of(indexedInput)).mod(zq.q);

            xys.add(x);
            xys.add(y);
        }
        checkState(xys.size() == Parameters.M * 2);
        return xys;
    }

    public static InstructionTableAndHardenedPassword generate(int numFeatures, Password pwd, Random random) {
        return generate(
                new Feature[numFeatures], // no distinguishing features yet
                pwd,
                random);
    }

    public static InstructionTableAndHardenedPassword generate(Feature[] features,
                                                               Password pwd,
                                                               Random random) {
        checkNotNull(features);
        checkArgument(features.length > 0);

        Zq zq = new Zq(BigInteger.probablePrime(Parameters.Q_LEN, random));
        Polynomial f = new RandomPolynomial(random).nextPolynomial(features.length, zq.q);

        BigInteger hpwd = f.apply(BigInteger.ZERO);

        byte[] r = new byte[R_LEN_IN_BYTES];
        random.nextBytes(r);

        G g = G.forSaltedPassword(r, pwd, zq);
        P p = new P(r, zq);

        Entry[] table = new Entry[features.length];
        for (int i = 0; i < table.length; i++) {
            BigInteger twoI = BigInteger.valueOf(2*i);
            BigInteger y0 = f.apply(p.of(twoI));
            BigInteger alpha = y0.add(g.of(twoI)).mod(zq.q);
            if (features[i] == BETA) {
                alpha = zq.randomElementNotEqualTo(alpha, random);
            }

            BigInteger twoIPlusOne = twoI.add(BigInteger.ONE);
            BigInteger y1 = f.apply(p.of(twoIPlusOne));
            BigInteger beta = y1.add(g.of(twoIPlusOne)).mod(zq.q);
            if (features[i] == ALPHA) {
                beta = zq.randomElementNotEqualTo(beta, random);
            }
            table[i] = new Entry(alpha, beta);
        }

        return new InstructionTableAndHardenedPassword(new InstructionTable(zq, r, table), hpwd);
    }

    public static final class InstructionTableAndHardenedPassword {
        public final InstructionTable table;
        public final BigInteger hpwd;

        public InstructionTableAndHardenedPassword(InstructionTable table, BigInteger hpwd) {
            this.table = table;
            this.hpwd = hpwd;
        }
    }

    public static class Entry {
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

    public void write(OutputStream outputStream) throws IOException {
        ZqOutputStream out = new ZqOutputStream(new BufferedOutputStream(outputStream));
        out.writeBigInteger(zq.q);
        out.write(r);
        for (Entry entry : table) {
            entry.write(out);
        }
        out.flush();
    }

    public static InstructionTable read(InputStream inputStream) throws IOException {
        ZqInputStream in = new ZqInputStream(new BufferedInputStream(inputStream));
        try {
            BigInteger q = in.readBigInteger();
            byte[] r = new byte[R_LEN_IN_BYTES];
            in.read(r);

            Entry[] entries = new Entry[Parameters.M];
            int i = 0;
            while (true) {
                Entry entry = Entry.read(in);
                if (entry == null) {
                    break;
                }
                entries[i++] = entry;
            }
            return new InstructionTable(new Zq(q), r, entries);
        } finally {
            in.close();
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(String.format("q=%s\n", zq.q.toString(16)));
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
        if (!zq.equals(that.zq)) return false;
        if (!Arrays.equals(r, that.r)) return false;
        if (!Arrays.equals(table, that.table)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = zq.hashCode();
        result = 31 * result + Arrays.hashCode(r);
        result = 31 * result + Arrays.hashCode(table);
        return result;
    }
}
