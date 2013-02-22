package seclogin;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import scala.tools.jline.console.ConsoleReader;

import java.security.SecureRandom;
import java.util.Random;

public class Console {

    public static void main(String[] args) throws Exception {

        ArgumentParser parser = ArgumentParsers
            .newArgumentParser(SecLogin.class.getSimpleName())
            .defaultHelp(true)
            .description("Password hardening proof-of-concept");

        parser.addArgument("-a", "--add")
            .help("Add specified user");

        Namespace ns;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            throw exit(1);
        }

        Random random = new SecureRandom();
        SecLogin secLogin = new SecLogin(
            new ConsoleReader(),
            random,
            QuestionBank.createDefault()
        );

        String usernameToAdd = ns.getString("add");
        if (usernameToAdd != null) {
            secLogin.addUser(usernameToAdd);
            System.out.printf("Added user %s.\n", usernameToAdd);
            throw exit(0);
        }

        secLogin.prompt();

    }

    static RuntimeException exit(int exitCode) {
        System.exit(exitCode);
        return null;
    }

}
