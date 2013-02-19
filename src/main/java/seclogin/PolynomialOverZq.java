package seclogin;

import java.math.BigInteger;

public class PolynomialOverZq {

    private final BigInteger[] coeffs;
    public final Zq zq;

    PolynomialOverZq(BigInteger[] coeffs, Zq zq) {
        this.coeffs = coeffs;
        this.zq = zq;
    }

    public BigInteger y(BigInteger x) {
        BigInteger y = BigInteger.ZERO;
        for (int exponent = 0; exponent < coeffs.length; exponent++) {
            y = y.add(coeffs[exponent].multiply(x.modPow(BigInteger.valueOf(exponent), zq.q))).mod(zq.q);
        }
        return y;
    }
}
