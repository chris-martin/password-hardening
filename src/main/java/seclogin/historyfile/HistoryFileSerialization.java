package seclogin.historyfile;

import java.nio.ByteBuffer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Serializes/deserializes (unencrypted) history files.
 */
class HistoryFileSerialization {

    private static final int INT_SIZE_IN_BYTES = Integer.SIZE / Byte.SIZE;
    private static final int DOUBLE_SIZE_IN_BYTES = Double.SIZE / Byte.SIZE;

    /** Serializes this history file (unencrypted). */
    byte[] toByteArray(HistoryFile historyFile) {
        byte[] plaintext = new byte[sizeInBytes(historyFile)];

        int offset = 0;

        byte[] userHash = historyFile.userHash;
        System.arraycopy(userHash, 0, plaintext, offset, userHash.length);
        offset += userHash.length;

        int maxNrOfMeasurements = historyFile.measurements.length;
        ByteBuffer.wrap(plaintext, offset, INT_SIZE_IN_BYTES).putInt(maxNrOfMeasurements);
        offset += INT_SIZE_IN_BYTES;

        int nrOfFeatures = historyFile.measurements[0].length;
        ByteBuffer.wrap(plaintext, offset, INT_SIZE_IN_BYTES).putInt(nrOfFeatures);
        offset += INT_SIZE_IN_BYTES;

        ByteBuffer.wrap(plaintext, offset, INT_SIZE_IN_BYTES).putInt(historyFile.nrOfMeasurements);
        offset += INT_SIZE_IN_BYTES;

        double[][] measurements = historyFile.measurements;
        for (int j = 0; j < measurements.length; j++) {
            checkState(measurements[j].length == nrOfFeatures);
            for (int i = 0; i < measurements[j].length; i++) {
                ByteBuffer.wrap(plaintext, offset, DOUBLE_SIZE_IN_BYTES).putDouble(measurements[j][i]);
                offset += DOUBLE_SIZE_IN_BYTES;
            }
        }
        checkState(offset == plaintext.length);

        return plaintext;
    }

    /** Returns the total number of bytes needed to serialize this history file. */
    private int sizeInBytes(HistoryFile historyFile) {
        checkArgument(historyFile.measurements.length > 0);
        return (HistoryFile.USER_HASH_FN.bits() / Byte.SIZE) +
                (3 * INT_SIZE_IN_BYTES) +
                (historyFile.measurements.length * historyFile.measurements[0].length * DOUBLE_SIZE_IN_BYTES);
    }

    /** Deserializes the given unencrypted history file. */
    HistoryFile fromByteArray(byte[] plaintext) {
        int offset = 0;

        byte[] userHash = new byte[HistoryFile.USER_HASH_FN.bits() / Byte.SIZE];
        System.arraycopy(plaintext, offset, userHash, 0, userHash.length);
        offset += userHash.length;

        int maxNrOfMeasurements = ByteBuffer.wrap(plaintext, offset, INT_SIZE_IN_BYTES).getInt();
        offset += INT_SIZE_IN_BYTES;

        int nrOfFeatures = ByteBuffer.wrap(plaintext, offset, INT_SIZE_IN_BYTES).getInt();
        offset += INT_SIZE_IN_BYTES;

        int nrOfMeasurements = ByteBuffer.wrap(plaintext, offset, INT_SIZE_IN_BYTES).getInt();
        offset += INT_SIZE_IN_BYTES;

        double[][] measurements = new double[maxNrOfMeasurements][nrOfFeatures];
        for (int j = 0; j < measurements.length; j++) {
            for (int i = 0; i < measurements[j].length; i++) {
                measurements[j][i] = ByteBuffer.wrap(plaintext, offset, DOUBLE_SIZE_IN_BYTES).getDouble();
                offset += DOUBLE_SIZE_IN_BYTES;
            }
        }

        return new HistoryFile(userHash, nrOfMeasurements, measurements);
    }
}
