package seclogin.math;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static seclogin.math.BigIntUtil.sumMod;

/** A polynomial represented by its `coefficients` in the standard (monomial) basis.
  */
public class Polynomial {

    private final List<BigInteger> coefficients;
    private final Mod q;

    /**
     * @param coefficients the coefficients for ''(1, x, x^2^, x^3^, ...)'' respectively
     */
    public Polynomial(List<BigInteger> coefficients, Mod q) {
        this.coefficients = coefficients;
        this.q = q;
    }

    public List<BigInteger> coefficients() {
        return coefficients;
    }

    /**
     * @return the number of coefficients (the degree of the polynomial plus 1,
     * assuming nonzero coefficients)
     */
    public int n() {
        return coefficients.size();
    }

    /**
     * @return the order of the polynomial (assuming nonzero coefficients), equal to ''n-1''.
     */
    public int order() {
        return n() - 1;
    }

    /**
     * @return ''f(x)'', the value of this function ''f'' at `x`.
     */
    public BigInteger apply(BigInteger x) {
        List<BigInteger> terms = new ArrayList<BigInteger>();
        for (int i = 0; i < coefficients.size(); i++) {
            terms.add((coefficients.get(i).multiply(q.pow(x, i))).mod(q.q));
        }
        return sumMod(terms, q.q);
    }

    /**
     * @return for each ''x'' in `xs`, the point ''(x, f(x))''
     */
    public List<Point> points(Iterable<BigInteger> xs) {
        List<Point> points = new ArrayList<Point>();
        for (BigInteger x : xs) {
            points.add(new Point(x, apply(x)));
        }
        return points;
    }

}
