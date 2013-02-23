package seclogin.crypto;

import seclogin.HardenedPassword;
import seclogin.Password;

import javax.crypto.SecretKey;

/**
 * A block cipher that knows how to derive keys for itself from a
 * hardened password and from a salted, regular password.
 */
public interface Cipher {

    SecretKey deriveKey(HardenedPassword hpwd);

    SecretKey deriveKey(byte[] salt, Password password);

    byte[] encrypt(SecretKey key, byte[] plaintext);

    byte[] decrypt(SecretKey key, byte[] plaintext);
}
