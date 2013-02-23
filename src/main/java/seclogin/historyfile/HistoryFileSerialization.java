package seclogin.historyfile;

import java.nio.ByteBuffer;

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

        ByteBuffer.wrap(plaintext, offset, INT_SIZE_IN_BYTES).putInt(historyFile.params.maxNrOfMeasurements);
        offset += INT_SIZE_IN_BYTES;

        int nrOfFeatures = historyFile.params.nrOfFeatures;
        ByteBuffer.wrap(plaintext, offset, INT_SIZE_IN_BYTES).putInt(nrOfFeatures);
        offset += INT_SIZE_IN_BYTES;

        ByteBuffer.wrap(plaintext, offset, INT_SIZE_IN_BYTES).putInt(historyFile.nrOfMeasurements);
        offset += INT_SIZE_IN_BYTES;

        double[][] measurements = historyFile.measurements;
        checkState(measurements.length == historyFile.params.maxNrOfMeasurements);
        for (int j = 0; j < measurements.length; j++) {
            for (int i = 0; i < measurements[j].length; i++) {
                checkState(measurements[j].length == nrOfFeatures);
                int measurementOffset = offset + doubleByteOffset(j, i, nrOfFeatures);
                ByteBuffer.wrap(plaintext, measurementOffset, DOUBLE_SIZE_IN_BYTES).putDouble(measurements[j][i]);
            }
        }

        return plaintext;
    }

    /** Returns the total number of bytes needed to serialize this history file. */
    private int sizeInBytes(HistoryFile historyFile) {
        return (HistoryFile.USER_HASH_FN.bits() / Byte.SIZE) +
                (3 * INT_SIZE_IN_BYTES) +
                (historyFile.params.maxNrOfMeasurements * historyFile.params.nrOfFeatures * DOUBLE_SIZE_IN_BYTES);
    }

    /** Returns the byte offset at which to store the measurement value at the given indices. */
    private static int doubleByteOffset(int featureIndex, int measurementIndex, int nrOfFeatures) {
        return DOUBLE_SIZE_IN_BYTES * ((featureIndex * nrOfFeatures) + measurementIndex);
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
                int measurementOffset = offset + doubleByteOffset(j, i, nrOfFeatures);
                measurements[j][i] = ByteBuffer.wrap(plaintext, measurementOffset, DOUBLE_SIZE_IN_BYTES).getDouble();
            }
        }

        HistoryFileParams params = new HistoryFileParams(maxNrOfMeasurements, nrOfFeatures);
        return new HistoryFile(params, userHash, nrOfMeasurements, measurements);
    }
}
