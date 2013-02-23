package seclogin;

import java.util.Arrays;

import static com.google.common.base.Preconditions.checkNotNull;

/** A regular, unhardened, character sequence password. */
public class Password {

    private final char[] password;

    public Password(String password) {
        this.password = checkNotNull(password).toCharArray();
    }

    public char[] asCharArray() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Password that = (Password) o;

        if (!Arrays.equals(password, that.password)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(password);
    }
}
