package seclogin.math;

import seclogin.SecurityParameters;

import java.math.BigInteger;
import java.util.Random;

/** Generates random probable primes of bit length specified by {@link SecurityParameters#Q_LEN}. */
public class RandomQ {

    private final Random random;

    public RandomQ(Random random) {
        this.random = random;
    }

    /** Returns the next random probable primes of bit length specified by {@link SecurityParameters#Q_LEN}. */
    public Mod nextQ() {
        return new Mod(BigInteger.probablePrime(SecurityParameters.Q_LEN, random));
    }
}
