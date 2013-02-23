package seclogin;

import static com.google.common.base.Preconditions.checkNotNull;

public class User {

    public final String user;

    public User(String user) {
        this.user = checkNotNull(user);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user1 = (User) o;

        if (!user.equals(user1.user)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return user.hashCode();
    }

    @Override
    public String toString() {
        return user;
    }
}
