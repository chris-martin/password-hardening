package seclogin.math;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static seclogin.math.BigIntUtil.*;

/** Interpolation for the unique minimal-degree polynomial
 * ''f : ℤ,,q,, → ℤ,,q,,'' containing all of the `points`.
 */
public class Interpolation {

    private final List<Point> points;
    private final Mod q;

    /**
     * @param points ''(x, y)'' pairs that reside in ''f''
     * @param q size of the field over which ''f'' is defined
     */
    public Interpolation(List<Point> points, Mod q) {
        this.points = points;
        this.q = q;
    }

    /**
     * @return the value of the interpolated polynomial evaluated at `0`
     */
    public BigInteger yIntercept() {
        List<BigInteger> terms = new ArrayList<BigInteger>();
        for (int i = 0; i < points.size(); i++) {
            terms.add((points.get(i).y.multiply(lagrangeCoefficient(i))).mod(q.q));
        }
        return sumMod(terms, q.q);
    }

    /**
     * @return the `i`^th^ Lagrange coefficient
     */
    public BigInteger lagrangeCoefficient(int i) {
        List<BigInteger> terms = new ArrayList<BigInteger>();
        for (int j = 0; j < points.size(); j++) {
            if (i != j) {
                terms.add(q.divide(
                    points.get(j).x,
                    points.get(j).x.subtract(points.get(i).x)
                ));
            }
        }
        return productMod(terms, q.q);
    }

}
