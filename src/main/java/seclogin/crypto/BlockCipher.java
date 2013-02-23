package seclogin.crypto;

import seclogin.HardenedPassword;
import seclogin.Password;

import javax.crypto.SecretKey;

/**
 * A block cipher that knows how to derive keys for itself from a
 * hardened password and from a salted, regular password.
 */
public interface BlockCipher {

    /** Derives a key for this cipher from the given hardened password. */
    SecretKey deriveKey(HardenedPassword hpwd);

    /** Derived a key for this cipher from the given salt and password. */
    SecretKey deriveKey(byte[] salt, Password password);

    /** Encrypts the given plaintext with the given key. */
    byte[] encrypt(SecretKey key, byte[] plaintext);

    /**
     * Decrypts the given ciphertext with the given key.
     *
     * @throws IndecipherableException if the given ciphertext cannot be decrypted properly with the given key
     */
    byte[] decrypt(SecretKey key, byte[] ciphertext) throws IndecipherableException;
}
