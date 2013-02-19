package seclogin;

import java.math.BigInteger;
import java.util.Random;

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

    public BigInteger randomElementNotEqualTo(BigInteger forbiddenValue, Random random) {
        BigInteger value;
        while ((value = randomElement(random)).equals(forbiddenValue));
        return value;
    }

    public PolynomialOverZq randomPolynomial(int order, Random random) {
        BigInteger[] coeffs = new BigInteger[order + 1];
        for (int i = 0; i < coeffs.length; i++) {
            coeffs[i] = randomElement(random);
        }
        return new PolynomialOverZq(coeffs, this);
    }

    public G g(Password pwd) {
        return G.forPassword(pwd, this);
    }
}
