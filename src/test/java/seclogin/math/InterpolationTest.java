package seclogin.math;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class InterpolationTest {

    @Test
    public void test6randomCoefficientsMod89() throws Exception {
        testInterpolation(
            6,
            new Mod(BigInteger.valueOf(89))
        );
    }

    @Test
    public void test200randomCoefficientsModA160bitPrime() throws Exception {
        testInterpolation(
            200,
            new Mod(new BigInteger("a85d364b1faa7c32a7e0c1676a26e50afb131443", 16))
        );
    }

    @Test
    public void testTwoFixedPoints() throws Exception {

        BigInteger i = new Interpolation(
            Lists.newArrayList(new Point(2, 2), new Point(4, 3)),
            new Mod(BigInteger.valueOf(11))
        ).yIntercept();

        assertEquals(BigInteger.valueOf(1), i);
    }

    void testInterpolation(int m, Mod q) throws Exception {

        SecureRandom r = SecureRandom.getInstance("SHA1PRNG");
        r.setSeed(new byte[0]);
        RandomPolynomial rp = new RandomPolynomial(r, q);

        Polynomial polynomial = rp.nextPolynomial(m);
        List<Point> points = rp.nextPoints(polynomial, m);

        BigInteger i = new Interpolation(points, q).yIntercept();

        assertEquals(polynomial.coefficients().get(0), i);
    }

}