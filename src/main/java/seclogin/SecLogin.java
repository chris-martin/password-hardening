package seclogin;

import java.util.Random;

/** The entry point to SecLogin. */
public class SecLogin {

    private final UserInterface userInterface;
    private final UserStatePersistence userStatePersistence;
    private final Random random;
    private final QuestionBank questionBank;
    private final Authenticator authenticator;
    private final MeasurementParams[] measurementParams;
    private final HistoryFileParams historyFileParams;

    public SecLogin(UserInterface userInterface, UserStatePersistence userStatePersistence,
                    Random random, QuestionBank questionBank) {

        int nrOfFeatures = questionBank.getQuestions().size();
        historyFileParams = new HistoryFileParams(2, nrOfFeatures);
        this.userInterface = userInterface;
        this.userStatePersistence = userStatePersistence;
        this.random = random;
        this.questionBank = questionBank;
        measurementParams = questionBank.measurementParams();
        authenticator = new Authenticator(random, measurementParams, historyFileParams);
    }

    public void prompt() {

        String user = userInterface.ask("login: ");
        String password = readPassword();
        double[] measurements = askQuestions();

        UserState userState = userStatePersistence.read(user, measurementParams);

        if (userState != null) {
            userState = authenticator.authenticate(userState, password, measurements);
        }

        boolean loginCorrect = userState != null;

        if (loginCorrect) {
            userStatePersistence.write(userState);
        }

        userInterface.tell(loginCorrect ? "Login success." : "Login failure.");
        userInterface.tell("");
    }

    private String readPassword() {
        return userInterface.askSecret("password: ");
    }

    double[] askQuestions() {
        double[] measurements = new double[historyFileParams.nrOfFeatures()];
        int i = 0;
        for (Question question : questionBank) {
            measurements[i++] = askQuestion(question);
        }
        return measurements;
    }

    private double askQuestion(Question question) {
        while (true) {
            String answer = userInterface.ask(question.question() + " ");
            try {
                return Double.parseDouble(answer);
            } catch (NumberFormatException e) {
                userInterface.tell("Answer must be numeric.");
            }
        }
    }

    public void addUser(String user) {
        String password = readPassword();
        UserState userState = generateNewUserState(user, password);
        userStatePersistence.write(userState);
    }

    private UserState generateNewUserState(String user, String password) {
        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
                InstructionTable.generate(password, measurementParams, null, random);
        HistoryFile.Encrypted historyFile =
            HistoryFile.emptyHistoryFile(user, historyFileParams).encrypt(tableAndHpwd.hpwd);
        return new UserState(user, tableAndHpwd.table, historyFile);
    }

}
