package seclogin;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import org.apache.commons.math3.stat.descriptive.StatisticalSummary;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static seclogin.Feature.ALPHA;
import static seclogin.Feature.BETA;

/* A history file for a particular user. */
public class HistoryFile {

    private static final HashFunction USER_HASH_FN = Hashing.sha256();

    private final byte[] userHash;
    private final int numMeasurements;
    private final double[][] measurements;

    private HistoryFile(byte[] userHash, int numMeasurements, double[][] measurements) {
        this.userHash = userHash;
        this.numMeasurements = numMeasurements;
        this.measurements = measurements;
    }

    public static HistoryFile emptyHistoryFile(String user) {
        byte[] userHash = USER_HASH_FN.hashString(user).asBytes();
        return new HistoryFile(
                userHash,
                0,
                new double[Parameters.H][Parameters.M]);
    }

    public Encrypted encrypt(BigInteger hpwd) {
        return new Encrypted(asEncryptedByteArray(hpwd));
    }

    public static Encrypted read(InputStream inputStream) throws IOException {
        BufferedInputStream in = new BufferedInputStream(inputStream);
        byte[] ciphertext;
        try {
            ciphertext = ByteStreams.toByteArray(in);
        } finally {
            in.close();
        }
        return new Encrypted(ciphertext);
    }

    private static HistoryFile fromEncryptedByteArray(byte[] ciphertext, BigInteger hpwd)
            throws IndecipherableHistoryFileException {
        byte[] plaintext;
        try {
            plaintext = Crypto.aes128Decrypt(hpwd, ciphertext);
        } catch (Exception e) {
            throw new IndecipherableHistoryFileException(e);
        }
        return fromByteArray(plaintext);
    }

    private byte[] asEncryptedByteArray(BigInteger hpwd) {
        byte[] plaintext = asByteArray();
        return Crypto.aes128Encrypt(hpwd, plaintext);
    }

    byte[] asByteArray() {
        byte[] plaintext = new byte[sizeInBytes()];

        int offset = 0;

        System.arraycopy(userHash, 0, plaintext, offset, userHash.length);
        offset += userHash.length;

        ByteBuffer.wrap(plaintext, offset, Integer.SIZE/Byte.SIZE).putInt(numMeasurements);
        offset += Integer.SIZE/Byte.SIZE;

        checkState(measurements.length == Parameters.H);
        for (int j = 0; j < measurements.length; j++) {
            for (int i = 0; i < measurements[j].length; i++) {
                ByteBuffer.wrap(plaintext, offset + doubleByteOffset(j, i), DOUBLE_SIZE_IN_BYTES).putDouble(measurements[j][i]);
            }
        }

        return plaintext;
    }

    private int sizeInBytes() {
        return (USER_HASH_FN.bits() / Byte.SIZE) +
                (Integer.SIZE/Byte.SIZE) +
                (Parameters.H * Parameters.M * DOUBLE_SIZE_IN_BYTES);
    }

    private static int doubleByteOffset(int j, int i) {
        return (DOUBLE_SIZE_IN_BYTES * ((j* Parameters.M)+i));
    }

    private static final int DOUBLE_SIZE_IN_BYTES = Double.SIZE / Byte.SIZE;

    static HistoryFile fromByteArray(byte[] plaintext) {
        int offset = 0;

        byte[] userHash = new byte[USER_HASH_FN.bits() / Byte.SIZE];
        System.arraycopy(plaintext, offset, userHash, 0, userHash.length);
        offset += userHash.length;

        int numMeasurements = ByteBuffer.wrap(plaintext, offset, Integer.SIZE/Byte.SIZE).getInt();
        offset += Integer.SIZE/Byte.SIZE;

        double[][] measurements = new double[Parameters.H][Parameters.M];
        for (int j = 0; j < measurements.length; j++) {
            for (int i = 0; i < measurements[j].length; i++) {
                measurements[j][i] = ByteBuffer.wrap(plaintext, offset + doubleByteOffset(j, i), Double.SIZE/Byte.SIZE).getDouble();
            }
        }

        return new HistoryFile(userHash, numMeasurements, measurements);
    }

    public boolean userHashEquals(String user) {
        return Arrays.equals(userHash, USER_HASH_FN.hashString(user).asBytes());
    }

    public HistoryFile withMostRecentMeasurements(double[] mostRecentMeasurements) {
        checkArgument(mostRecentMeasurements.length == Parameters.M);
        double[][] shiftedMeasurements = new double[measurements.length][];
        shiftedMeasurements[0] = mostRecentMeasurements;
        System.arraycopy(measurements, 0, shiftedMeasurements, 1, shiftedMeasurements.length - 1);
        return new HistoryFile(userHash, Math.max(Parameters.H, numMeasurements + 1), shiftedMeasurements);
    }

    public Feature[] deriveFeatures(QuestionBank questionBank) {
        checkArgument(questionBank.getQuestions().size() == Parameters.M);

        SummaryStatistics[] stats = calculateStats();
        Feature[] features = new Feature[Parameters.M];
        for (int i = 0; i < features.length; i++) {
            StatisticalSummary userStats = stats[i];
            double mu = userStats.getMean();
            double sigma = userStats.getStandardDeviation();
            double t = questionBank.getQuestions().get(i).getResponseMean();
            if (numMeasurements < Parameters.H || Math.abs(mu - t) > (Parameters.K * sigma)) {
                features[i] = mu < t ? ALPHA : BETA;
            }
        }
        return features;
    }

    private SummaryStatistics[] calculateStats() {
        SummaryStatistics[] stats = new SummaryStatistics[Parameters.M];
        for (int i = 0; i < stats.length; i++) {
            stats[i] = new SummaryStatistics();
            for (double[] measurement : measurements) {
                stats[i].addValue(measurement[i]);
            }
        }
        return stats;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HistoryFile that = (HistoryFile) o;

        if (!Arrays.equals(userHash, that.userHash)) return false;
        if (!Arrays.deepEquals(measurements, that.measurements)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(userHash);
        result = 31 * result + Arrays.deepHashCode(measurements);
        return result;
    }

    @Override
    public String toString() {
        return "HistoryFile{" +
                "userHash=" + Arrays.toString(userHash) +
                ", measurements=" + (measurements == null ? null : Arrays.deepToString(measurements)) +
                '}';
    }

    /** An encrypted history file. */
    public static class Encrypted {
        private final byte[] ciphertext;

        private Encrypted(byte[] ciphertext) {
            this.ciphertext = ciphertext;
        }

        public HistoryFile decrypt(BigInteger hpwd) throws IndecipherableHistoryFileException {
            return HistoryFile.fromEncryptedByteArray(ciphertext, hpwd);
        }

        public void write(OutputStream outputStream) throws IOException {
            BufferedOutputStream out = new BufferedOutputStream(outputStream);
            out.write(ciphertext);
            out.flush();
            out.close();
        }
    }
}
