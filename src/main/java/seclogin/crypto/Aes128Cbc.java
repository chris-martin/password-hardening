package seclogin.crypto;

import com.google.common.base.Throwables;
import com.google.common.hash.Hashing;
import seclogin.HardenedPassword;
import seclogin.Password;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.spec.KeySpec;

/**
 * AES-128-CBC.
 */
public class Aes128Cbc implements Cipher {

    private static final int KEY_LEN = 128;
    private static final int KEY_LEN_IN_BYTE = KEY_LEN / Byte.SIZE;

    @Override
    public SecretKey deriveKey(HardenedPassword hpwd) {
        byte[] hash = Hashing.sha256().hashBytes(hpwd.toByteArray()).asBytes();
        return new SecretKeySpec(hash, 0, KEY_LEN_IN_BYTE, "AES"); // truncate hash to 128-bits for AES key
    }

    @Override
    public SecretKey deriveKey(byte[] salt, Password password) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.asCharArray(), salt, 65536, KEY_LEN);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    @Override
    public byte[] encrypt(SecretKey key, byte[] plaintext) {
        return aes(javax.crypto.Cipher.ENCRYPT_MODE, key, plaintext);
    }

    @Override
    public byte[] decrypt(SecretKey key, byte[] plaintext) {
        return aes(javax.crypto.Cipher.DECRYPT_MODE, key, plaintext);
    }

    private static byte[] aes(int mode, SecretKey key, byte[] input) {
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(new byte[16]); // use zero IV
            cipher.init(mode, key, iv);
            return cipher.doFinal(input);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }
}
