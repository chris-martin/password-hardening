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
    private BigInteger maxInput = BigInteger.valueOf(-1);

    public P(byte[] key, Zq zq) {
        random = new Random(new BigInteger(key).longValue()); // TODO horrible...find a real PRP or PRG
        this.zq = zq;
        definition = HashBiMap.create();
    }

    public BigInteger of(BigInteger input) {
        BigInteger output = definition.get(input);
        if (output == null) {
            for (BigInteger i = maxInput.add(BigInteger.ONE); i.compareTo(input) <= 0; i = i.add(BigInteger.ONE)) {
                while (definition.containsValue(output = zq.randomElement(random)));
                definition.put(i, output);
            }
            maxInput = input;
        }
        return output;
    }
}
