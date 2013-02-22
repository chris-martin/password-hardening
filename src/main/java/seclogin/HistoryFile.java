package seclogin;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import javax.annotation.Nullable;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/*
 * A history file for a particular user that contains measurements (responses to questions)
 * from a fixed number of previously successful login attempts.
 */
public class HistoryFile {

    private static final HashFunction USER_HASH_FN = Hashing.sha256();

    private final HistoryFileParams historyFileParams;
    private final byte[] userHash;
    private final int numMeasurements;
    private final double[][] measurements;

    private HistoryFile(HistoryFileParams historyFileParams, byte[] userHash, int numMeasurements, double[][] measurements) {
        this.historyFileParams = historyFileParams;
        this.userHash = userHash;
        this.numMeasurements = numMeasurements;
        this.measurements = measurements;
    }

    /** Returns an empty history file. */
    public static HistoryFile emptyHistoryFile(String user, HistoryFileParams params) {
        byte[] userHash = USER_HASH_FN.hashString(user).asBytes();
        return new HistoryFile(params, userHash, 0,
            new double[params.maxNrOfEntries()][params.nrOfFeatures()]);
    }

    /** Encrypts this history file using the given hardened password. */
    public Encrypted encrypt(BigInteger hpwd) {
        byte[] plaintext = asByteArray();
        byte[] ciphertext = Crypto.aesEncrypt(hpwd, plaintext);
        return new Encrypted(ciphertext);
    }

    /** Reads the encrypted history file supplied by the given stream. */
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

    /** An encrypted history file. Must be decrypted with the corresponding hardened password. */
    public static class Encrypted {
        private final byte[] ciphertext;

        private Encrypted(byte[] ciphertext) {
            this.ciphertext = ciphertext;
        }

        /** Decrypts the given encrypted history file with the given hardened password. */
        public HistoryFile decrypt(BigInteger hpwd, HistoryFileParams params) throws IndecipherableHistoryFileException {
            return HistoryFile.fromEncryptedByteArray(ciphertext, hpwd, params);
        }

        public void write(OutputStream outputStream) throws IOException {
            BufferedOutputStream out = new BufferedOutputStream(outputStream);
            out.write(ciphertext);
            out.flush();
            out.close();
        }
    }

    /** Decrypts the given encrypted history file with the given hardened password. */
    private static HistoryFile fromEncryptedByteArray(byte[] ciphertext, BigInteger hpwd, HistoryFileParams params)
            throws IndecipherableHistoryFileException {
        byte[] plaintext;
        try {
            plaintext = Crypto.aesDecrypt(hpwd, ciphertext);
        } catch (Exception e) {
            throw new IndecipherableHistoryFileException(e);
        }
        return fromByteArray(plaintext, params);
    }

    /** Serializes this history file (unencrypted). */
    byte[] asByteArray() {
        byte[] plaintext = new byte[sizeInBytes()];

        int offset = 0;

        System.arraycopy(userHash, 0, plaintext, offset, userHash.length);
        offset += userHash.length;

        ByteBuffer.wrap(plaintext, offset, Integer.SIZE/Byte.SIZE).putInt(numMeasurements);
        offset += Integer.SIZE/Byte.SIZE;

        checkState(measurements.length == historyFileParams.maxNrOfEntries());
        for (int j = 0; j < measurements.length; j++) {
            for (int i = 0; i < measurements[j].length; i++) {
                ByteBuffer.wrap(plaintext, offset + doubleByteOffset(j, i, historyFileParams), DOUBLE_SIZE_IN_BYTES).putDouble(measurements[j][i]);
            }
        }

        return plaintext;
    }

    /** Returns the total number of bytes needed to serialize this history file. */
    private int sizeInBytes() {
        return (USER_HASH_FN.bits() / Byte.SIZE) +
                (Integer.SIZE/Byte.SIZE) +
                (historyFileParams.maxNrOfEntries() * historyFileParams.nrOfFeatures() * DOUBLE_SIZE_IN_BYTES);
    }

    /** Returns the byte offset at which to store the measurement value at the given indices. */
    private static int doubleByteOffset(int featureIndex, int measurementIndex, HistoryFileParams params) {
        return DOUBLE_SIZE_IN_BYTES * ((featureIndex * params.nrOfFeatures()) + measurementIndex);
    }

    private static final int DOUBLE_SIZE_IN_BYTES = Double.SIZE / Byte.SIZE;

    /** Deserializes the given unencrypted history file. */
    static HistoryFile fromByteArray(byte[] plaintext, HistoryFileParams params) {
        int offset = 0;

        byte[] userHash = new byte[USER_HASH_FN.bits() / Byte.SIZE];
        System.arraycopy(plaintext, offset, userHash, 0, userHash.length);
        offset += userHash.length;

        int numMeasurements = ByteBuffer.wrap(plaintext, offset, Integer.SIZE/Byte.SIZE).getInt();
        offset += Integer.SIZE/Byte.SIZE;

        double[][] measurements = new double[params.maxNrOfEntries()][params.nrOfFeatures()];
        for (int j = 0; j < measurements.length; j++) {
            for (int i = 0; i < measurements[j].length; i++) {
                measurements[j][i] = ByteBuffer.wrap(plaintext, offset + doubleByteOffset(j, i, params), Double.SIZE/Byte.SIZE).getDouble();
            }
        }

        return new HistoryFile(params, userHash, numMeasurements, measurements);
    }

    /** Returns whether this history file's user hash matches that of the given user. */
    public boolean userHashEquals(String user) {
        return Arrays.equals(userHash, USER_HASH_FN.hashString(user).asBytes());
    }

    /**
     * Returns a new history file that includes the given most recent measurements
     * and excludes the least recent measurements if the file is full.
     */
    public HistoryFile withMostRecentMeasurements(double[] mostRecentMeasurements) {
        checkArgument(mostRecentMeasurements.length == historyFileParams.nrOfFeatures());
        double[][] shiftedMeasurements = new double[measurements.length][];
        shiftedMeasurements[0] = mostRecentMeasurements;
        System.arraycopy(measurements, 0, shiftedMeasurements, 1, shiftedMeasurements.length - 1);
        return new HistoryFile(historyFileParams, userHash, Math.min(historyFileParams.maxNrOfEntries(), numMeasurements + 1), shiftedMeasurements);
    }

    /**
     * Returns statistics of the measurements in this history file, or null if the file is not yet full, i.e.,
     * if the user has not successfully logged at least as many times as measurements can fit in this history file.
     */
    @Nullable
    public MeasurementStats[] calculateStats() {
        if (numMeasurements != measurements.length) {
            return null;
        }

        MeasurementStats[] stats = new MeasurementStats[historyFileParams.nrOfFeatures()];
        for (int i = 0; i < stats.length; i++) {
            SummaryStatistics stat = new SummaryStatistics();
            for (double[] measurement : measurements) {
                stat.addValue(measurement[i]);
            }
            stats[i] = new MeasurementStats(stat.getMean(), stat.getStandardDeviation());
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
                ", numMeasurements=" + numMeasurements +
                ", measurements=" + (measurements == null ? null : Arrays.asList(measurements)) +
                '}';
    }
}
