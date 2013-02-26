package seclogin.math;

import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Represents ''ℤ,,q,,'', the field of integers ''[0, q)''.
 */
public class Mod {

    public final BigInteger q;

    /**
     * @param q the size of the field
     */
    public Mod(BigInteger q) {
        checkNotNull(q);
        this.q = q;
    }

    /**
     * @return true iff `a` is an element of ''ℤ,,q,,''
     */
    public boolean contains(BigInteger a) {
        return a.compareTo(BigInteger.ZERO) >= 0 && a.compareTo(q) < 0;
    }

    /**
     * @return ''`a`/`b` (mod `q`)''
     */
    public BigInteger divide(BigInteger a, BigInteger b) {
        return a.multiply(b.modInverse(q)).mod(q);
    }

    /**
     *  @return ''`base`^`exp`^ (mod `q`)''
     */
    public BigInteger pow(BigInteger base, BigInteger exp) {
        return base.modPow(exp, q);
    }

    /**
     *  @return ''`base`^`exp`^ (mod `q`)''
     */
    public BigInteger pow(BigInteger base, int exp) {
        return pow(base, BigInteger.valueOf(exp));
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mod mod = (Mod) o;

        if (q != null ? !q.equals(mod.q) : mod.q != null) return false;

        return true;
    }

    public int hashCode() {
        return q != null ? q.hashCode() : 0;
    }

    public String toString() {
        return "mod 0x" + q.toString(16);
    }

}
