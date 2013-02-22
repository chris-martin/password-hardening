package seclogin.math;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Adapts a `Random` for use with `BigInt`s in a finite field `Mod` q.
 */
public class RandomBigIntModQ {

    private final Random r;
    final Mod q;

    public RandomBigIntModQ(Random r, Mod q) {
        this.r = r;
        this.q = q;
    }

    /**
     * @return a uniformly random element of ''ℤ,,q,,''
     */
    public BigInteger nextBigIntModQ() {
        int bits = q.q.bitLength();
        while (true) {
            BigInteger x = new BigInteger(bits, r);
            if (x.compareTo(q.q) < 0) return x;
        }
    }

    /**
     * @return a uniformly random element of ''ℤ,,q,,'' not equal to the given value
     */
    public BigInteger nextBigIntegerModQNotEqualTo(BigInteger forbidden) {
        while (true) {
            BigInteger x = nextBigIntModQ();
            if (!x.equals(forbidden)) return x;
        }
    }

    /**
     * @return a uniformly random size-`n` subset of ''ℤ,,q,,''
     */
    Set<BigInteger> nextUniqueBigIntsModQ(int n) {
        Set<BigInteger> set = new HashSet<BigInteger>();
        while (set.size() < n) set.add(nextBigIntModQ());
        return set;
    }

}

