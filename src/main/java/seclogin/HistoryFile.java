package seclogin;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Arrays;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

public class HistoryFile {

    private static final HashFunction USER_HASH_FN = Hashing.sha256();

    private final byte[] userHash;
    private final Feature[][] features;

    private HistoryFile(byte[] userHash, Feature[][] features) {
        this.userHash = userHash;
        this.features = features;
    }

    public static HistoryFile emptyHistoryFile(String user) {
        byte[] userHash = USER_HASH_FN.hashString(user).asBytes();
        return new HistoryFile(
                userHash,
                new Feature[Parameters.H][Parameters.M]);
    }

    public void write(OutputStream outputStream, BigInteger hpwd) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(outputStream);
        out.write(asEncryptedByteArray(hpwd));
        out.flush();
        out.close();
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

    private byte[] asByteArray() {
        byte[] plaintext = new byte[sizeInBytes()];

        int offset = 0;

        System.arraycopy(userHash, 0, plaintext, offset, userHash.length);
        offset += userHash.length;

        checkState(features.length == Parameters.H);
        for (int j = 0; j < features.length; j++) {
            for (int i = 0; i < features[j].length; i++) {
                plaintext[offset + (j * Parameters.M) + i] = Feature.asByte(features[j][i]);
            }
        }

        return plaintext;
    }

    private int sizeInBytes() {
        return (USER_HASH_FN.bits() / Byte.SIZE) + (Parameters.H * Parameters.M);
    }

    private static HistoryFile fromByteArray(byte[] plaintext) {
        int offset = 0;

        byte[] userHash = new byte[USER_HASH_FN.bits() / Byte.SIZE];
        System.arraycopy(plaintext, offset, userHash, 0, userHash.length);
        offset += userHash.length;

        Feature[][] features = new Feature[Parameters.H][Parameters.M];
        for (int j = 0; j < features.length; j++) {
            for (int i = 0; i < features[j].length; i++) {
                features[j][i] = Feature.fromByte(plaintext[offset + (j * Parameters.M) + i]);
            }
        }

        return new HistoryFile(userHash, features);
    }

    public boolean userHashEquals(String user) {
        return Arrays.equals(userHash, USER_HASH_FN.hashString(user).asBytes());
    }

    public HistoryFile withMostRecentFeatures(Feature[] mostRecentFeatures) {
        checkArgument(mostRecentFeatures.length == Parameters.M);
        Feature[][] shiftedFeatures = new Feature[features.length][];
        shiftedFeatures[0] = mostRecentFeatures;
        System.arraycopy(features, 0, shiftedFeatures, 1, shiftedFeatures.length - 1);
        return new HistoryFile(userHash, shiftedFeatures);
    }

    public Feature[] getFeatures(int attempt) {
        checkArgument(attempt >= 0 && attempt < features.length);
        return features[attempt];
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
        if (!Arrays.deepEquals(features, that.features)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(userHash);
        result = 31 * result + Arrays.deepHashCode(features);
        return result;
    }

    public static class Encrypted {
        private final byte[] ciphertext;

        private Encrypted(byte[] ciphertext) {
            this.ciphertext = ciphertext;
        }

        public HistoryFile decrypt(BigInteger hpwd) throws IndecipherableHistoryFileException {
            return HistoryFile.fromEncryptedByteArray(ciphertext, hpwd);
        }
    }
}
