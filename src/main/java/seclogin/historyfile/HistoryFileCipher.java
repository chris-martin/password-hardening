package seclogin.historyfile;

import seclogin.HardenedPassword;
import seclogin.User;
import seclogin.crypto.Aes128Cbc;
import seclogin.crypto.BlockCipher;
import seclogin.crypto.IndecipherableException;

import javax.crypto.SecretKey;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * History file cipher. Encrypts/decrypts history files.
 */
public class HistoryFileCipher {

    private final BlockCipher cipher;
    private final HistoryFileSerialization serialization = new HistoryFileSerialization();

    /** Constructs a {@link HistoryFileCipher} using {@link Aes128Cbc} as the block cipher. */
    public HistoryFileCipher() {
        this(new Aes128Cbc());
    }

    /** Constructs a {@link HistoryFileCipher} using the given block cipher. */
    public HistoryFileCipher(BlockCipher cipher) {
        this.cipher = checkNotNull(cipher);
    }

    /** Encrypts this history file using the given hardened password. */
    public EncryptedHistoryFile encrypt(HistoryFile historyFile, HardenedPassword hpwd) {
        checkNotNull(historyFile);
        checkNotNull(hpwd);

        byte[] plaintext = serialization.toByteArray(historyFile);
        SecretKey key = cipher.deriveKey(hpwd);
        byte[] ciphertext = cipher.encrypt(key, plaintext);
        return new EncryptedHistoryFile(ciphertext);
    }

    /**
     * Decrypts the given encrypted history file for the given user with the given hardened password.
     *
     * @throws IndecipherableHistoryFileException if the password and/or user are incorrect
     */
    public HistoryFile decrypt(EncryptedHistoryFile encryptedHistoryFile, HardenedPassword hpwd, User user)
            throws IndecipherableHistoryFileException {
        checkNotNull(encryptedHistoryFile);
        checkNotNull(hpwd);
        checkNotNull(user);

        SecretKey key = cipher.deriveKey(hpwd);
        byte[] plaintext;
        try {
            plaintext = cipher.decrypt(key, encryptedHistoryFile.ciphertext);
        } catch (IndecipherableException e) {
            throw new IndecipherableHistoryFileException(e); // the hardened password is incorrect
        }
        HistoryFile historyFile = serialization.fromByteArray(plaintext);
        if (!historyFile.verifyUserHash(user)) {
            throw new IndecipherableHistoryFileException(); // the hardened password and/or user are incorrect
        }
        return historyFile;
    }
}
