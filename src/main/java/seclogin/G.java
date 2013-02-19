package seclogin;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.hash.Hashing;

public class G {

    private final byte[] key;
    private final Zq zq;

    private G(byte[] key, Zq zq) {
        this.key = key;
        this.zq = zq;
    }

    public static G forPassword(Password pwd, Zq zq) {
        // TODO salt password
        return new G(Hashing.sha1().hashBytes(pwd.asBytes()).asBytes(), zq);
    }

    public BigInteger g(BigInteger input) {
        return new BigInteger(hmacSha1(input.toByteArray())).mod(zq.q);
    }

    private byte[] hmacSha1(byte[] input) {
        Mac mac;
        try {
            mac = Mac.getInstance(hashAlgorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        SecretKeySpec keySpec = new SecretKeySpec(key, hashAlgorithm);

        try {
            mac.init(keySpec);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        return mac.doFinal(input);
    }

    private static final String hashAlgorithm = "HMacSHA1";
}
