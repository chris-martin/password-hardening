package seclogin.historyfile;

import java.util.Arrays;

/** An encrypted history file. Must be decrypted with the corresponding hardened password. */
public class EncryptedHistoryFile {

    final byte[] ciphertext;

    EncryptedHistoryFile(byte[] ciphertext) {
        this.ciphertext = ciphertext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EncryptedHistoryFile that = (EncryptedHistoryFile) o;

        if (!Arrays.equals(ciphertext, that.ciphertext)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(ciphertext);
    }
}
