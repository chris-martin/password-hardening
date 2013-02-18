package seclogin;

import java.io.Console;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

public class SecLogin {

    private final Console console;
    private final Random random;
    private final QuestionBank questionBank;

    public SecLogin(Console console, Random random, QuestionBank questionBank) {
        this.console = console;
        this.random = random;
        this.questionBank = questionBank;
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

    public void addUser(String user) {
        char[] rawPassword;
        while ((rawPassword = console.readPassword("password: ")) == null || rawPassword.length == 0);

        Password password = new Password(rawPassword);
        try {
            InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
                    InstructionTable.init(questionBank.getQuestions().size(), password, random);
            password.destroy();

            System.out.println(tableAndHpwd.table);

            try {
                FileOutputStream out = new FileOutputStream(instructionTableFile(user));
                tableAndHpwd.table.serialize(out);
                out.close();
            } catch (IOException e) {
                System.err.println("Could not write instruction table.");
                System.exit(1);
            }

            // TODO write fixed-size history file
        } finally {
            password.destroy();
        }
    }

    private File instructionTableFile(String user) {
        return new File("instruction-table-" + user);
    }

    public static void main(String[] args) {Console console = System.console();
        if (console == null) {
            System.err.println("Must be run from console.");
            System.exit(1);
            return;
        }

        ArgumentParser parser = ArgumentParsers.newArgumentParser(SecLogin.class.getSimpleName())
                .defaultHelp(true)
                .description("Password hardening proof-of-concept");
        parser.addArgument("-a", "--add")
                .help("Add specified user");

        Namespace ns;
        try {
            ns = parser.parseArgs(args);
        } catch (ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
            return;
        }

        Random random = new SecureRandom();
        SecLogin secLogin = new SecLogin(console, random, SAMPLE_QUESTION_BANK);

        String usernameToAdd = ns.getString("add");
        if (usernameToAdd != null) {
            secLogin.addUser(usernameToAdd);
            return;
        }

        secLogin.prompt();
    }

    private static final QuestionBank SAMPLE_QUESTION_BANK = new QuestionBank(
            ImmutableList.of(
                    new Question("How far (in miles) are you from the Georgia Tech campus?", 1),
                    new Question("How long (in minutes) do you anticipate being logged in during this session?", 20),
                    new Question("How many emails will you send during this session?", 2)
            )
    );
}
