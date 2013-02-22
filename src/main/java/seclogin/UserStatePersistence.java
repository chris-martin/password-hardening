package seclogin;

public interface UserStatePersistence {

    UserState read(String user, MeasurementParams[] measurementParams);

    void write(UserState userState);

}
