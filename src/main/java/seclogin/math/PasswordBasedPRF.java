package seclogin.math;

import seclogin.Crypto;

import javax.crypto.SecretKey;
import java.math.BigInteger;

/**
 * A PRF over Z_q, keyed on a salted password.
 *
 * Implemented by delegating to AES-128 with key generated by
 * PBKDF2-HMAC-SHA1 from the provided salt and password.
 */
public class PasswordBasedPRF {

    private final SecretKey key;
    private final Zq zq;

    private PasswordBasedPRF(SecretKey key, Zq zq) {
        this.key = key;
        this.zq = zq;
    }

    public static PasswordBasedPRF forSaltedPassword(byte[] salt, String password, Zq zq) {
        return new PasswordBasedPRF(Crypto.deriveAes128Key(salt, password.toCharArray()), zq);
    }

    public BigInteger of(int input) {
        return new BigInteger(Crypto.aes128Encrypt(key, BigInteger.valueOf(input).toByteArray())).mod(zq.q);
    }
}