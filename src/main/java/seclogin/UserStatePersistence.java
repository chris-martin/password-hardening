package seclogin;

public interface UserStatePersistence {

    UserState read(User user, MeasurementParams[] measurementParams);

    void write(UserState userState);

}
