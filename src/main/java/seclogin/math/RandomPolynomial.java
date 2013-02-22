package seclogin.math;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

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
        List<BigInteger> coefficients = new ArrayList<BigInteger>();
        for (int i = 0; i < n; i++) {
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
