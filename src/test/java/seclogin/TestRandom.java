package seclogin;

import java.security.SecureRandom;
import java.util.Random;

public class TestRandom {

    /** Returns a deterministic {@link Random} always seeded with the same value. */
    public static Random random() throws Exception {
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
        random.setSeed(new byte[0]);
        return random;
    }
}
