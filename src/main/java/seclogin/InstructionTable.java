package seclogin;

import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import seclogin.io.ZqInputStream;
import seclogin.io.ZqOutputStream;
import seclogin.math.*;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.*;
import static seclogin.FeatureValue.ALPHA;
import static seclogin.FeatureValue.BETA;

public class InstructionTable {

    public static final int R_LEN_IN_BYTES = Parameters.R_LEN / Byte.SIZE;

    private final Zq zq;
    private final byte[] r;
    private final List<Entry> table;

    private int nrOfFeatures() {
        return table.size();
    }

    private InstructionTable(Zq zq, byte[] r, List<Entry> table) {
        checkArgument(r.length == R_LEN_IN_BYTES);
        this.zq = zq;
        this.r = r;
        this.table = table;
    }

    public BigInteger interpolateHpwd(Password pwd, List<FeatureValue> featureValues) {
        List<BigInteger> xys = xys(pwd, featureValues);
        return new Interpolation(xys, zq.q).yIntercept().bigInteger();
    }

    private List<BigInteger> xys(Password pwd, List<FeatureValue> featureValues) {
        checkArgument(featureValues.size() == table.size());

        G g = G.forSaltedPassword(r, pwd, zq);
        SparsePRP p = new SparsePRP(r, zq.q);

        List<BigInteger> xys = Lists.newArrayListWithCapacity(featureValues.size() * 2);
        for (int i = 0; i < featureValues.size(); i++) {
            FeatureValue featureValue = featureValues.get(i);
            checkNotNull(featureValue);

            Entry entry = table.get(i);

            int indexedInput = featureValue == ALPHA ? (2*i) : ((2*i)+1);
            BigInteger x = p.apply(indexedInput).bigInteger();
            BigInteger y = (featureValue == ALPHA ? entry.alpha : entry.beta)
                    .subtract(g.of(BigInteger.valueOf(indexedInput)))
                    .mod(zq.q);

            xys.add(x);
            xys.add(y);
        }
        checkState(xys.size() == nrOfFeatures() * 2);
        return xys;
    }

    public static InstructionTableAndHardenedPassword generate(List<FeatureValue> featureValues,
                                                               Password pwd,
                                                               Random random) {
        checkNotNull(featureValues);

        Zq zq = new Zq(BigInteger.probablePrime(Parameters.Q_LEN, random));
        Polynomial f = new RandomPolynomial(random).nextPolynomial(featureValues.size(), zq.q);

        BigInteger hpwd = f.apply(BigInteger.ZERO);

        byte[] r = new byte[R_LEN_IN_BYTES];
        random.nextBytes(r);

        G g = G.forSaltedPassword(r, pwd, zq);
        SparsePRP p = new SparsePRP(r, zq.q);

        List<Entry> table = Lists.newArrayListWithCapacity(featureValues.size());
        for (int i = 0; i < featureValues.size(); i++) {
            BigInteger y0 = f.apply(p.apply(2*i)).bigInteger();
            BigInteger alpha = y0.add(g.of(BigInteger.valueOf(2*i))).mod(zq.q);
            if (featureValues.get(i) == BETA) {
                alpha = zq.randomElementNotEqualTo(alpha, random);
            }

            BigInteger y1 = f.apply(p.apply((2*i)+1)).bigInteger();
            BigInteger beta = y1.add(g.of(BigInteger.valueOf((2*i)+1))).mod(zq.q);
            if (featureValues.get(i) == ALPHA) {
                beta = zq.randomElementNotEqualTo(beta, random);
            }
            table.add(new Entry(alpha, beta));
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

    public static InstructionTable read(InputStream inputStream, int nrOfFeatures) throws IOException {
        ZqInputStream in = new ZqInputStream(new BufferedInputStream(inputStream));
        try {
            BigInteger q = in.readBigInteger();
            byte[] r = new byte[R_LEN_IN_BYTES];
            in.read(r);

            List<Entry> entries = Lists.newArrayListWithCapacity(nrOfFeatures);
            while (true) {
                Entry entry = Entry.read(in);
                if (entry == null) {
                    break;
                }
                entries.add(entry);
            }
            checkState(entries.size() == nrOfFeatures);
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
        for (int i = 0; i < table.size(); i++) {
            Entry entry = table.get(i);
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
        if (!table.equals(that.table)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = zq.hashCode();
        result = 31 * result + Arrays.hashCode(r);
        result = 31 * result + table.hashCode();
        return result;
    }
}
