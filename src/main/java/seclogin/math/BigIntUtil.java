package seclogin.math;

import java.math.BigInteger;

public class BigIntUtil {

    private BigIntUtil() { }

    static BigInteger sumMod(Iterable<BigInteger> xs, BigInteger mod) {
        BigInteger sum = BigInteger.ZERO;
        for (BigInteger x : xs) {
            sum = sum.add(x).mod(mod);
        }
        return sum;
    }

    static BigInteger productMod(Iterable<BigInteger> xs, BigInteger mod) {
        BigInteger product = BigInteger.ONE;
        for (BigInteger x : xs) {
            product = product.multiply(x).mod(mod);
        }
        return product;
    }

}
