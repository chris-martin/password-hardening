package seclogin;

public interface UserInterface {

    String ask(String prompt);

    String askSecret(String prompt);

    void tell(String message);

}
