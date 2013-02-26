package seclogin.math;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

/** Adapts a `Random` for use with `Polynomial`s.
  */
public class RandomPolynomial {

    private final RandomBigIntModQ r;

    public RandomPolynomial(RandomBigIntModQ r) {
        this.r = r;
    }

    public RandomPolynomial(Random r, Mod q) {
        this(new RandomBigIntModQ(r, q));
    }

    /**
     * @return a random polynomial with `n` coefficients
     * @param n the number of coefficients in the polynomial
     */
    public Polynomial nextPolynomial(int n) {
        return nextPolynomial(r.nextBigIntModQ(), n);
    }

    /**
     * @return a random polynomial with `n` coefficients, the first of which (i.e., the y-intercept) is given
     * @param n the number of coefficients in the polynomial
     */
    public Polynomial nextPolynomial(BigInteger yIntercept, int n) {
        checkArgument(n >= 1);
        List<BigInteger> coefficients = new ArrayList<BigInteger>(n);
        coefficients.add(yIntercept);
        for (int i = 1; i < n; i++) {
            coefficients.add(r.nextBigIntModQ());
        }
        return new Polynomial(coefficients, r.q);
    }

    /**
     * @return a random sampling of `nrOfPoints` distinct points from `polynomial`
     * @param polynomial a polynomial that will be evaluated
     * @param nrOfPoints the number of evaluations and the size of the returned collection
     */
    public List<Point> nextPoints(Polynomial polynomial, int nrOfPoints) {
        Set<BigInteger> xs = r.nextUniqueBigIntsModQ(nrOfPoints);
        return polynomial.points(xs);
    }

}
