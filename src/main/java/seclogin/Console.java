package seclogin;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import scala.tools.jline.console.ConsoleReader;

import java.io.IOException;
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

        parser.addArgument("-v", "--verbose")
                .action(Arguments.storeTrue());

        Namespace ns;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            throw exit(1);
        }

        boolean verbose = ns.getBoolean("verbose");

        Random random = new SecureRandom();
        SecLogin secLogin = new SecLogin(
            new ConsoleUI(),
            new UserStateFilesystemPersistence(),
            random,
            QuestionBank.createDefault()
        );

        try {
            String usernameToAdd = ns.getString("add");
            if (usernameToAdd != null) {
                secLogin.addUser(usernameToAdd);
                System.out.printf("Added user %s.\n", usernameToAdd);
                throw exit(0);
            }

            secLogin.prompt();
        } catch (RuntimeException e) {
            if (verbose) {
                e.printStackTrace();
            } else {
                System.err.println(e.getMessage());
            }
            throw exit(1);
        }

    }

    static RuntimeException exit(int exitCode) {
        System.exit(exitCode);
        return null;
    }

    static class ConsoleUI implements UserInterface {

        private final ConsoleReader console;
        private final Thread shutdownHook;

        ConsoleUI(final ConsoleReader console) {
            this.console = console;
            shutdownHook = new Thread(new Runnable() {
                @Override
                public void run() {
                    restoreTerminal();
                }
            });
            Runtime.getRuntime().addShutdownHook(shutdownHook);
        }

        @Override
        protected void finalize() throws Throwable {
            restoreTerminal();
            Runtime.getRuntime().removeShutdownHook(shutdownHook);
            super.finalize();
        }

        private void restoreTerminal() {
            try {
                console.getTerminal().restore();
            } catch (Throwable ignored) {
            }
        }

        ConsoleUI() {
            this(newConsoleReader());
        }

        static ConsoleReader newConsoleReader() {
            try {
                return new ConsoleReader();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private String ask(String prompt, Character mask) {
            try {
                return console.readLine(prompt + " ", mask);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public String ask(String prompt) {
            return ask(prompt, null);
        }

        public String askSecret(String prompt) {
            return ask(prompt, ConsoleReader.NULL_MASK);
        }

        public void tell(String message) {
            try {
                console.println(message);
                console.flush();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }

}
