package seclogin;

import javax.annotation.Nullable;

public interface UserStatePersistence {

    /** Returns the user state for the given user, or null if no state exists for the user. */
    @Nullable
    UserState read(User user);

    void write(UserState userState);

}
