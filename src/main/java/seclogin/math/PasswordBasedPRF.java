package seclogin.math;

import seclogin.Password;
import seclogin.crypto.Aes128Cbc;
import seclogin.crypto.BlockCipher;

import javax.crypto.SecretKey;
import java.math.BigInteger;

/**
 * A PRF over Z_q, keyed on a salted password.
 *
 * Implemented by delegating to AES-128 with key generated by
 * PBKDF2-HMAC-SHA1 from the provided salt and password.
 */
public class PasswordBasedPRF {

    private static final BlockCipher cipher = new Aes128Cbc();

    private final SecretKey key;
    private final Mod q;

    private PasswordBasedPRF(SecretKey key, Mod q) {
        this.key = key;
        this.q = q;
    }

    public static PasswordBasedPRF forSaltedPassword(byte[] salt, Password password, Mod q) {
        return new PasswordBasedPRF(cipher.deriveKey(salt, password), q);
    }

    public BigInteger of(int input) {
        return new BigInteger(cipher.encrypt(key, BigInteger.valueOf(input).toByteArray())).mod(q.q);
    }
}
