package passwordhardening;

import java.math.BigInteger;
import java.security.SecureRandom;

public class GeneratePrime {

    private GeneratePrime() {}

    public static void main(String[] args) {
        int bits = args.length == 0 ? 160 : Integer.parseInt(args[0]);
        BigInteger prime = new BigInteger(bits, 1000, new SecureRandom());
        System.out.println(prime.toString(16));
    }
}
