package seclogin.math;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

/** A pseudo-random permutation (PRP) in ''ℤ,,q,,'' based on a `PRG`.
  * This PRP is considered "sparse" because it is efficient only over small inputs.
  */
public class SparsePRP {

    private final RandomBigIntModQ randomBigIntModQ;

    private final ArrayList<BigInteger> sequence = new ArrayList<BigInteger>();
    private final HashSet<BigInteger> set = new HashSet<BigInteger>();

    /**
     * Constructor using the default PRG with a given `key`.
     */
    public SparsePRP(byte[] key, Mod q) {
        SecureRandom prg;
        try {
            prg = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        prg.setSeed(key);
        randomBigIntModQ = new RandomBigIntModQ(prg, q);
    }

    public BigInteger apply(int i) {
        while (sequence.size() <= i) {
            BigInteger x = generateAnother();
            sequence.add(x);
            set.add(x);
        }
        return sequence.get(i);
    }

    private BigInteger generateAnother() {
        while (true) {
            BigInteger x = randomBigIntModQ.nextBigIntModQ();
            if (!set.contains(x)) return x;
        }
    }

}
