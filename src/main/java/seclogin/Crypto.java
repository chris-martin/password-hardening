package seclogin;

import com.google.common.base.Throwables;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.spec.KeySpec;

public class Crypto {

    private Crypto() {}

    public static byte[] aes128Encrypt(BigInteger key, byte[] plaintext) {
        return aes128(Cipher.ENCRYPT_MODE, key, plaintext);
    }

    public static byte[] aes128Decrypt(BigInteger key, byte[] ciphertext) {
        return aes128(Cipher.DECRYPT_MODE, key, ciphertext);
    }

    private static byte[] aes128(int mode, BigInteger keyAsInt, byte[] input) {
        byte[] rawKey = keyAsInt.toByteArray();
        SecretKey key = new SecretKeySpec(rawKey, rawKey.length - 16, 16, "AES"); // need only 128-bits of hpwd for AES key
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

    public static SecretKey deriveAes128Key(byte[] salt, char[] password) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password, salt, 65536, 128);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public static byte[] aes128Encrypt(SecretKey key, byte[] input) {
        return aes(Cipher.ENCRYPT_MODE, key, input);
    }
}
