package passwordhardening;

import java.io.Console;

import com.google.common.base.Strings;

public class Login {

    private final Console console;

    public Login(Console console) {
        this.console = console;
    }

    public void prompt() {
        String user;
        while (Strings.isNullOrEmpty(user = console.readLine("login: ")));

        Password password = new Password(console.readPassword("password: "));

        boolean success = false;
        try {
            success = authenticate(user, password);
        } finally {
            password.destroy();
        }

        if (success) {
            System.out.println("Login correct.");
        } else {
            System.err.println("Login incorrect.");
        }
        System.out.println();
    }

    private boolean authenticate(String user, Password password) {
        return false;
    }

    public static void main(String[] args) {
        Console console = System.console();
        if (console == null) {
            System.err.println("Must be run from console.");
            System.exit(1);
            return;
        }

        Login login = new Login(console);
        while (true) {
            login.prompt();
        }
    }
}
