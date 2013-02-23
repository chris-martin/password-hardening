package seclogin.crypto;

import com.google.common.hash.Hashing;
import seclogin.HardenedPassword;
import seclogin.Password;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * AES-128-CBC.
 */
public class Aes128Cbc implements BlockCipher {

    private static final int KEY_LEN = 128;
    private static final int KEY_LEN_IN_BYTE = KEY_LEN / Byte.SIZE;

    @Override
    public SecretKey deriveKey(HardenedPassword hpwd) {
        checkNotNull(hpwd);

        byte[] hash = Hashing.sha256().hashBytes(hpwd.toByteArray()).asBytes();
        return new SecretKeySpec(hash, 0, KEY_LEN_IN_BYTE, "AES"); // truncate hash to 128-bits for AES key
    }

    @Override
    public SecretKey deriveKey(byte[] salt, Password password) {
        checkNotNull(salt);
        checkArgument(salt.length > 0);
        checkNotNull(password);

        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            KeySpec spec = new PBEKeySpec(password.asCharArray(), salt, 65536, KEY_LEN);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] encrypt(SecretKey key, byte[] plaintext) {
        checkNotNull(key);
        checkNotNull(plaintext);

        try {
            return aes(Cipher.ENCRYPT_MODE, key, plaintext);
        } catch (BadPaddingException e) {
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decrypt(SecretKey key, byte[] ciphertext) throws IndecipherableException {
        checkNotNull(key);
        checkNotNull(ciphertext);

        try {
            return aes(Cipher.DECRYPT_MODE, key, ciphertext);
        } catch (BadPaddingException e) {
            throw new IndecipherableException(e);
        } catch (IllegalBlockSizeException e) {
            throw new IndecipherableException(e);
        }
    }

    private static byte[] aes(int mode, SecretKey key, byte[] input)
            throws BadPaddingException, IllegalBlockSizeException {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(new byte[16]); // use zero IV
            cipher.init(mode, key, iv);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
        return cipher.doFinal(input);
    }
}
