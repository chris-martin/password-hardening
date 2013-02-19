package seclogin.math;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/** A pseudorandom permutation family over Z_q. */
public class P {

    private final Zq zq;
    private final Random random;

    private final BiMap<BigInteger, BigInteger> definition;

    public P(byte[] key, Zq zq) {
        random = new SecureRandom(key);
        this.zq = zq;
        definition = HashBiMap.create();
    }

    public BigInteger of(BigInteger input) {
        BigInteger output = definition.get(input);
        if (output == null) {
            while (definition.containsValue(output = zq.randomElement(random)));
        }
        definition.put(input, output);
        return output;
    }
}
