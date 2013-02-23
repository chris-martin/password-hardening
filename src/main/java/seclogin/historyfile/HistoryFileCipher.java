package seclogin.historyfile;

import seclogin.HardenedPassword;
import seclogin.User;
import seclogin.crypto.Aes128Cbc;
import seclogin.crypto.Cipher;

import javax.crypto.SecretKey;

/**
 * History file cipher. Encrypts/decrypts history files.
 */
public class HistoryFileCipher {

    private final Cipher cipher;
    private final HistoryFileSerialization serialization = new HistoryFileSerialization();

    public HistoryFileCipher() {
        this(new Aes128Cbc());
    }

    public HistoryFileCipher(Cipher cipher) {
        this.cipher = cipher;
    }

    /** Encrypts this history file using the given hardened password. */
    public EncryptedHistoryFile encrypt(HistoryFile historyFile, HardenedPassword hpwd) {
        byte[] plaintext = serialization.toByteArray(historyFile);
        SecretKey key = cipher.deriveKey(hpwd);
        byte[] ciphertext = cipher.encrypt(key, plaintext);
        return new EncryptedHistoryFile(ciphertext);
    }

    /**
     * Decrypts the given encrypted history file for the given user with the given hardened password.
     *
     * @throws IndecipherableHistoryFileException if either the password or user are incorrect.
     */
    public HistoryFile decrypt(EncryptedHistoryFile encryptedHistoryFile, HardenedPassword hpwd, User user)
            throws IndecipherableHistoryFileException {
        SecretKey key = cipher.deriveKey(hpwd);
        byte[] plaintext;
        try {
            plaintext = cipher.decrypt(key, encryptedHistoryFile.ciphertext);
        } catch (Exception e) {
            throw new IndecipherableHistoryFileException(e);
        }
        HistoryFile historyFile = serialization.fromByteArray(plaintext);
        if (!historyFile.verifyUserHash(user)) {
            throw new IndecipherableHistoryFileException();
        }
        return historyFile;
    }
}
