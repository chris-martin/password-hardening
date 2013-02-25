package seclogin;

import com.google.common.base.Strings;
import seclogin.crypto.Aes128Cbc;
import seclogin.crypto.BlockCipher;
import seclogin.historyfile.EncryptedHistoryFile;
import seclogin.historyfile.HistoryFile;
import seclogin.historyfile.HistoryFileCipher;
import seclogin.historyfile.HistoryFileParams;
import seclogin.instructiontable.Distinguishment;
import seclogin.instructiontable.InstructionTable;

import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

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
                    Random random, QuestionBank questionBank, int historyFileSize) {
        this.userInterface = userInterface;
        this.userStatePersistence = userStatePersistence;
        this.random = random;

        this.questionBank = questionBank;
        checkArgument(questionBank.nrOfQuestions() > 0);
        measurementParams = questionBank.measurementParams();
        int nrOfFeatures = measurementParams.length;
        checkState(nrOfFeatures == questionBank.nrOfQuestions());

        historyFileParams = new HistoryFileParams(historyFileSize, nrOfFeatures);

        BlockCipher cipher = new Aes128Cbc();
        historyFileCipher = new HistoryFileCipher(cipher);
        authenticator = new Authenticator(random, measurementParams, historyFileCipher
        );
    }

    public void prompt() {

        User user = askUser();
        Password password = askPassword();
        double[] measurements = askQuestions();

        UserState userState = userStatePersistence.read(user);

        if (userState != null) {
            userState = authenticator.authenticate(userState, password, measurements);
        }

        boolean loginCorrect = userState != null;

        if (loginCorrect) {
            userStatePersistence.write(userState);
        }

        userInterface.tell(loginCorrect ? UserInterface.Success : UserInterface.Failure);
    }

    private User askUser() {
        while (true) {
            String response = userInterface.ask(UserInterface.UserPrompt);
            if (!Strings.isNullOrEmpty(response)) {
                return new User(response);
            }
        }
    }

    private Password askPassword() {
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
            if (Strings.isNullOrEmpty(answer)) {
                return Double.NaN;
            }
            try {
                return Double.parseDouble(answer);
            } catch (NumberFormatException e) {
                userInterface.tell("Answer must be numeric.");
            }
        }
    }

    public void addUser(String user) {
        Password password = askPassword();
        UserState userState = generateNewUserState(new User(user), password);
        userStatePersistence.write(userState);
    }

    private UserState generateNewUserState(User user, Password password) {
        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
                InstructionTable.generate(password, new Distinguishment[measurementParams.length], random);
        HistoryFile emptyHistoryFile = HistoryFile.emptyHistoryFile(user, historyFileParams);
        EncryptedHistoryFile encryptedHistoryFile = historyFileCipher.encrypt(emptyHistoryFile, tableAndHpwd.hpwd);
        return new UserState(user, tableAndHpwd.table, encryptedHistoryFile);
    }

}
