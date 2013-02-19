package seclogin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class InstructionTable {

    private static final int Q_BIT_LENGTH = 160;
    private static final BigInteger TWO = BigInteger.valueOf(2);

    public enum FeatureDistinguishment { ALPHA, BETA }

    private final Zq zq;
    private final Entry[] table;

    private InstructionTable(Zq zq, Entry[] table) {
        this.zq = zq;
        this.table = table;
    }

    public static InstructionTableAndHardenedPassword generate(int numFeatures, Password pwd, Random random) {
        return generate(
                new FeatureDistinguishment[numFeatures], // no distinguishing features yet
                pwd,
                random);
    }

    public static InstructionTableAndHardenedPassword generate(FeatureDistinguishment[] features,
                                                               Password pwd,
                                                               Random random) {
        checkNotNull(features);
        checkArgument(features.length > 0);

        Zq zq = new Zq(BigInteger.probablePrime(Q_BIT_LENGTH, random));
        PolynomialOverZq f = zq.randomPolynomial(features.length - 1, random);

        BigInteger hpwd = f.y(BigInteger.ZERO);

        Entry[] table = new Entry[features.length];
        G gPwd = zq.g(pwd);
        for (int i = 0; i < table.length; i++) {
            BigInteger twoI = BigInteger.valueOf(i).multiply(TWO);
            BigInteger alpha = f.y(p(twoI)).add(gPwd.g(twoI));
            if (features[i] == FeatureDistinguishment.BETA) {
                alpha = zq.randomElementNotEqualTo(alpha, random);
            }

            BigInteger twoIPlusOne = twoI.add(BigInteger.ONE);
            BigInteger beta = f.y(p(twoIPlusOne)).add(gPwd.g(twoIPlusOne));
            if (features[i] == FeatureDistinguishment.ALPHA) {
                beta = zq.randomElementNotEqualTo(beta, random);
            }
            table[i] = new Entry(alpha, beta);
        }

        return new InstructionTableAndHardenedPassword(new InstructionTable(zq, table), hpwd);
    }

    public static final class InstructionTableAndHardenedPassword {
        public final InstructionTable table;
        public final BigInteger hpwd;

        public InstructionTableAndHardenedPassword(InstructionTable table, BigInteger hpwd) {
            this.table = table;
            this.hpwd = hpwd;
        }
    }

    private static BigInteger p(BigInteger x) {
        return x.add(BigInteger.ONE);
    }

    public static class Entry {
        public final BigInteger alpha;
        public final BigInteger beta;

        public Entry(BigInteger alpha, BigInteger beta) {
            this.alpha = alpha;
            this.beta = beta;
        }
    }

    public void serialize(OutputStream outputStream) {
        PrintWriter out = new PrintWriter(new OutputStreamWriter(outputStream));
        out.println(serialize(zq.q));
        for (Entry entry : table) {
            out.println(serialize(entry.alpha));
            out.println(serialize(entry.beta));
        }
        out.flush();
    }

    public static InstructionTable deserialize(InputStream inputStream) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
        BigInteger q = deserialize(in.readLine());
        List<Entry> entries = Lists.newArrayList();
        while (true) {
            String alphaStr = in.readLine();
            if (alphaStr == null) {
                break;
            }

            String betaStr = in.readLine();
            Entry entry = new Entry(deserialize(alphaStr), deserialize(betaStr));
            entries.add(entry);
        }
        return new InstructionTable(new Zq(q), entries.toArray(new Entry[entries.size()]));
    }

    private static String serialize(BigInteger i) {
        return i.toString(SERIALIZATION_RADIX);
    }
    private static BigInteger deserialize(String s) {
        return new BigInteger(s, SERIALIZATION_RADIX);
    }
    private static final int SERIALIZATION_RADIX = 16;

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(String.format("q=%s\n", zq.q.toString(16)));
        for (int i = 0; i < table.length; i++) {
            Entry entry = table[i];
            s.append(String.format("a_%d=%s   b_%d=%s\n",
                    i, entry.alpha.toString(16),
                    i, entry.beta.toString(16)));
        }
        return s.toString();
    }
}
