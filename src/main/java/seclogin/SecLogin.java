package seclogin;

import java.io.File;
import java.io.IOException;
import java.util.Random;

/** The entry point to SecLogin. */
public class SecLogin {

    private final UserInterface userInterface;
    private final Random random;
    private final QuestionBank questionBank;
    private final Authenticator authenticator;
    private final MeasurementParams[] measurementParams;
    private final HistoryFileParams historyFileParams;

    public SecLogin(UserInterface userInterface, Random random, QuestionBank questionBank) {
        int nrOfFeatures = questionBank.getQuestions().size();
        historyFileParams = new HistoryFileParams(2, nrOfFeatures);
        this.userInterface = userInterface;
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

        String user = userInterface.ask("login: ");
        String password = readPassword();
        double[] measurements = askQuestions();

        UserState userState = UserState.read(userStateDir(), user, measurementParams);

        if (userState != null) {
            userState = authenticator.authenticate(userState, password, measurements);
        }

        boolean loginCorrect = userState != null;

        if (loginCorrect) {
            userState.write(userStateDir());
        }

        userInterface.tell(loginCorrect ? "Login success." : "Login failure.");
        userInterface.tell("");
    }

    private String readPassword() throws IOException {
        return userInterface.askSecret("password: ");
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
            String answer = userInterface.ask(question.question() + " ");
            try {
                return Double.parseDouble(answer);
            } catch (NumberFormatException e) {
                userInterface.tell("Answer must be numeric.");
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
