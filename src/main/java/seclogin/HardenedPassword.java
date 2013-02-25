package seclogin;

import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkNotNull;

/** A hardened password. */
public class HardenedPassword {

    private final BigInteger hpwd;

    public HardenedPassword(BigInteger hpwd) {
        this.hpwd = checkNotNull(hpwd);
    }

    public byte[] toByteArray() {
        return hpwd.toByteArray();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HardenedPassword that = (HardenedPassword) o;

        if (!hpwd.equals(that.hpwd)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return hpwd.hashCode();
    }

    @Override
    public String toString() {
        return hpwd.toString(16);
    }
}
