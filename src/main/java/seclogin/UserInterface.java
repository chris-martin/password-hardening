package seclogin;

public interface UserInterface {

    String ask(String prompt);

    String askSecret(String prompt);

    void tell(String message);

    String UserPrompt = "login:";

    String PasswordPrompt = "password:";

    String Success = "\n  Login success.\n";

    String Failure = "\n  Login failure.\n";

}
