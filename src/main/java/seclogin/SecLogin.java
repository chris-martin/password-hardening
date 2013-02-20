package seclogin;

import scala.tools.jline.console.ConsoleReader;
import java.io.File;
import java.io.FileInputStream;
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

import static com.google.common.base.Preconditions.checkState;
import static seclogin.Feature.ALPHA;
import static seclogin.Feature.BETA;

public class SecLogin {

    private final ConsoleReader console;
    private final Random random;
    private final QuestionBank questionBank;

    public SecLogin(ConsoleReader console, Random random, QuestionBank questionBank) {
        checkState(questionBank.getQuestions().size() == Parameters.M);
        this.console = console;
        this.random = random;
        this.questionBank = questionBank;
    }

    public void prompt() throws IOException {
        String user;
        while (Strings.isNullOrEmpty(user = console.readLine("login: ")));

        InstructionTable instructionTable;
        try {
            FileInputStream in = new FileInputStream(instructionTableFile(user));
            instructionTable = InstructionTable.read(in);
            in.close();
        } catch (IOException e) {
            System.err.println("Could not read instruction table.");
            System.exit(1);
            return;
        }

        HistoryFile.Encrypted encryptedHistoryFile;
        try {
            FileInputStream in = new FileInputStream(historyFile(user));
            encryptedHistoryFile = HistoryFile.read(in);
            in.close();
        } catch (IOException e) {
            System.err.println("Could not read instruction table.");
            System.exit(1);
            return;
        }

        Password password = new Password(console.readLine("password: ", ConsoleReader.NULL_MASK).toCharArray());
        Feature[] features = askQuestions();

        Authenticator authenticator = new Authenticator(
                user,
                password,
                features,
                instructionTable,
                encryptedHistoryFile);

        boolean success = false;
        try {
            success = authenticator.authenticate();
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

    Feature[] askQuestions() throws IOException {
        Feature[] features = new Feature[Parameters.M];
        int i = 0;
        for (Question question : questionBank) {
            features[i++] = askQuestion(question);
        }
        return features;
    }

    private Feature askQuestion(Question question) throws IOException {
        double numericAnswer;
        while (true) {
            String answer = console.readLine(question.getQuestion() + " ");
            try {
                numericAnswer = Double.parseDouble(answer);
                break;
            } catch (NumberFormatException e) {
                System.err.println("Answer must be numeric.");
            }
        }
        return numericAnswer < question.getAverageResponse() ? ALPHA : BETA;
    }

    public void addUser(String user) throws IOException {
        char[] rawPassword;
        while ((rawPassword = console.readLine("password: ", ConsoleReader.NULL_MASK).toCharArray()) == null || rawPassword.length == 0);

        Password password = new Password(rawPassword);
        try {
            InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
                    InstructionTable.generate(questionBank.getQuestions().size(), password, random);
            password.destroy();

            try {
                FileOutputStream out = new FileOutputStream(instructionTableFile(user));
                tableAndHpwd.table.write(out);
                out.close();
            } catch (IOException e) {
                System.err.println("Could not write instruction table.");
                System.exit(1);
            }

            HistoryFile historyFile = HistoryFile.emptyHistoryFile(user);
            try {
                FileOutputStream out = new FileOutputStream(historyFile(user));
                historyFile.write(out, tableAndHpwd.hpwd);
                out.close();
            } catch (IOException e) {
                System.err.println("Could not write history file.");
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

    private File historyFile(String user) {
        return new File("history-file-" + user);
    }

    public static void main(String[] args) throws IOException {

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
        SecLogin secLogin = new SecLogin(new ConsoleReader(), random, SAMPLE_QUESTION_BANK);

        String usernameToAdd = ns.getString("add");
        if (usernameToAdd != null) {
            secLogin.addUser(usernameToAdd);
            System.out.printf("Added user %s.\n", usernameToAdd);
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
