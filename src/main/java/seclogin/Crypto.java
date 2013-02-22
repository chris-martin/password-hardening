package seclogin;

import com.google.common.base.Throwables;
import com.google.common.hash.Hashing;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.spec.KeySpec;

/** Cryptography, in particular AES-128 and password-based key derivation for it. */
public class Crypto {

    private Crypto() {}

    public static byte[] aesEncrypt(BigInteger key, byte[] plaintext) {
        return aes(Cipher.ENCRYPT_MODE, key, plaintext);
    }

    public static byte[] aesDecrypt(BigInteger key, byte[] ciphertext) {
        return aes(Cipher.DECRYPT_MODE, key, ciphertext);
    }

    private static byte[] aes(int mode, BigInteger keyAsInt, byte[] input) {
        byte[] rawKey = Hashing.sha256().hashBytes(keyAsInt.toByteArray()).asBytes();
        SecretKey key = new SecretKeySpec(rawKey, 0, 16, "AES"); // truncate key to 128-bits for AES
        return aes(mode, key, input);
    }

    private static byte[] aes(int mode, SecretKey key, byte[] input) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(new byte[16]); // use zero IV
            cipher.init(mode, key, iv);
            return cipher.doFinal(input);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static SecretKey deriveAesKey(byte[] salt, char[] password) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password, salt, 65536, 128);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static byte[] aesEncrypt(SecretKey key, byte[] input) {
        return aes(Cipher.ENCRYPT_MODE, key, input);
    }
}
