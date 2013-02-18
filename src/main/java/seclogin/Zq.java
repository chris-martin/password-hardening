package seclogin;

import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.google.common.hash.Hashing;

public class Zq {

    public final BigInteger q;
    private final int qBitLength;

    public Zq(BigInteger q) {
        this.q = q;
        qBitLength = q.bitLength();
    }

    public BigInteger randomElement(Random random) {
        BigInteger candidate;
        while ((candidate = new BigInteger(qBitLength, random)).compareTo(q) >= 0);
        return candidate;
    }

    public PolynomialOverZq randomPolynomial(int order, Random random) {
        BigInteger[] coeffs = new BigInteger[order + 1];
        for (int i = 0; i < coeffs.length; i++) {
            coeffs[i] = randomElement(random);
        }
        return new PolynomialOverZq(coeffs, this);
    }

    public BigInteger g(Password pwd, BigInteger input) {
        return new BigInteger(hmacSha1(Hashing.sha1().hashBytes(pwd.asBytes()).asBytes(), input.toByteArray())).mod(q);
    }

    private static byte[] hmacSha1(byte[] key, byte[] input) {
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
