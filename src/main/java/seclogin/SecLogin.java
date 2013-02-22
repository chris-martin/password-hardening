package seclogin;

import com.google.common.base.Strings;
import scala.tools.jline.console.ConsoleReader;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/** The entry point to SecLogin. */
public class SecLogin {

    private final ConsoleReader console;
    private final Random random;
    private final QuestionBank questionBank;
    private final Authenticator authenticator;
    private final MeasurementParams[] measurementParams;
    private final HistoryFileParams historyFileParams;

    public SecLogin(ConsoleReader console, Random random, QuestionBank questionBank) {
        int nrOfFeatures = questionBank.getQuestions().size();
        historyFileParams = new HistoryFileParams(2, nrOfFeatures);
        this.console = console;
        this.random = random;
        this.questionBank = questionBank;
        measurementParams = questionBank.measurementParams();
        authenticator = new Authenticator(
            random,
            measurementParams,
            historyFileParams
        );
    }

    public void prompt() throws IOException {
        String user;
        while (Strings.isNullOrEmpty(user = console.readLine("login: ")));

        String password = readPassword();
        double[] measurements = askQuestions();

        UserState userState = UserState.read(userStateDir(), user, measurementParams);

        if (userState != null) {
            userState = authenticator.authenticate(userState, password, measurements);
        }

        if (userState != null) {
            userState.write(userStateDir());
            System.out.println("Login correct.");
        } else {
            System.err.println("Login incorrect.");
        }
        System.out.println();
    }

    private String readPassword() throws IOException {
        return console.readLine("password: ", ConsoleReader.NULL_MASK);
    }

    double[] askQuestions() throws IOException {
        double[] measurements = new double[historyFileParams.nrOfFeatures()];
        int i = 0;
        for (Question question : questionBank) {
            measurements[i++] = askQuestion(question);
        }
        return measurements;
    }

    private double askQuestion(Question question) throws IOException {
        while (true) {
            String answer = console.readLine(question.question() + " ");
            try {
                return Double.parseDouble(answer);
            } catch (NumberFormatException e) {
                System.err.println("Answer must be numeric.");
            }
        }
    }

    public void addUser(String user) throws IOException {
        String password;
        while ((password = readPassword()) == null || password.isEmpty());

        UserState userState = generateNewUserState(user, password);
        userState.write(userStateDir());
    }

    private UserState generateNewUserState(String user, String password) {
        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
                InstructionTable.generate(password, measurementParams, null, random);
        HistoryFile.Encrypted historyFile =
            HistoryFile.emptyHistoryFile(user, historyFileParams).encrypt(tableAndHpwd.hpwd);
        return new UserState(user, tableAndHpwd.table, historyFile);
    }

    /** The directory in which to store user history files and instruction tables. */
    private File userStateDir() {
        File file = new File(".seclogin");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

}
