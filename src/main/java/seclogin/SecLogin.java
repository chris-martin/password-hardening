package seclogin;

import seclogin.crypto.Aes128Cbc;
import seclogin.crypto.Cipher;
import seclogin.historyfile.EncryptedHistoryFile;
import seclogin.historyfile.HistoryFile;
import seclogin.historyfile.HistoryFileCipher;
import seclogin.historyfile.HistoryFileParams;

import java.util.Random;

/** The entry point to SecLogin. */
public class SecLogin {

    private final UserInterface userInterface;
    private final UserStatePersistence userStatePersistence;
    private final Random random;
    private final QuestionBank questionBank;
    private final Authenticator authenticator;
    private final HistoryFileCipher historyFileCipher;
    private final MeasurementParams[] measurementParams;
    private final HistoryFileParams historyFileParams;

    public SecLogin(UserInterface userInterface, UserStatePersistence userStatePersistence,
                    Random random, QuestionBank questionBank) {

        this.userInterface = userInterface;
        this.userStatePersistence = userStatePersistence;
        this.random = random;
        this.questionBank = questionBank;
        measurementParams = questionBank.measurementParams();
        int nrOfFeatures = measurementParams.length;
        int historyFileSize = 2;
        historyFileParams = new HistoryFileParams(historyFileSize, nrOfFeatures);

        Cipher cipher = new Aes128Cbc();
        historyFileCipher = new HistoryFileCipher(cipher);
        authenticator = new Authenticator(random, measurementParams, historyFileCipher);
    }

    public void prompt() {

        User user = new User(userInterface.ask(UserInterface.UserPrompt));
        Password password = readPassword();
        double[] measurements = askQuestions();

        UserState userState = userStatePersistence.read(user, measurementParams);

        if (userState != null) {
            userState = authenticator.authenticate(userState, password, measurements);
        }

        boolean loginCorrect = userState != null;

        if (loginCorrect) {
            userStatePersistence.write(userState);
        }

        userInterface.tell(loginCorrect ? UserInterface.Success : UserInterface.Failure);
        userInterface.tell("");
    }

    private Password readPassword() {
        return new Password(userInterface.askSecret(UserInterface.PasswordPrompt));
    }

    double[] askQuestions() {
        double[] measurements = new double[measurementParams.length];
        int i = 0;
        for (Question question : questionBank) {
            measurements[i++] = askQuestion(question);
        }
        return measurements;
    }

    private double askQuestion(Question question) {
        while (true) {
            String answer = userInterface.ask(question.question());
            try {
                return Double.parseDouble(answer);
            } catch (NumberFormatException e) {
                userInterface.tell("Answer must be numeric.");
            }
        }
    }

    public void addUser(String user) {
        Password password = readPassword();
        UserState userState = generateNewUserState(new User(user), password);
        userStatePersistence.write(userState);
    }

    private UserState generateNewUserState(User user, Password password) {
        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
                InstructionTable.generate(password, measurementParams, null, random);
        HistoryFile emptyHistoryFile = HistoryFile.emptyHistoryFile(user, historyFileParams);
        EncryptedHistoryFile encryptedHistoryFile = historyFileCipher.encrypt(emptyHistoryFile, tableAndHpwd.hpwd);
        return new UserState(user, tableAndHpwd.table, encryptedHistoryFile);
    }

}
