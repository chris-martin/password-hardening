package seclogin.math;

import java.math.BigInteger;
import java.util.Random;

/** The set Z_q of integers [0, q). */
public class Zq {

    public final BigInteger q;

    public Zq(BigInteger q) {
        this.q = q;
    }

    /** Returns a random element of Z_q. */
    public BigInteger randomElement(Random random) {
        BigInteger candidate;
        while ((candidate = new BigInteger(q.bitLength(), random)).compareTo(q) >= 0);
        return candidate;
    }

    /** Returns a random elemtn of Z_q not equal to the given value. */
    public BigInteger randomElementNotEqualTo(BigInteger forbiddenValue, Random random) {
        BigInteger value;
        while ((value = randomElement(random)).equals(forbiddenValue));
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Zq that = (Zq) o;
        if (!q.equals(that.q)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return q.hashCode();
    }
}
