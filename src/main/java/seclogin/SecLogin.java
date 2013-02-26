package seclogin;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seclogin.crypto.Aes128Cbc;
import seclogin.crypto.BlockCipher;
import seclogin.historyfile.EncryptedHistoryFile;
import seclogin.historyfile.HistoryFile;
import seclogin.historyfile.HistoryFileCipher;
import seclogin.historyfile.HistoryFileParams;
import seclogin.instructiontable.Distinguishment;
import seclogin.instructiontable.InstructionTable;
import seclogin.instructiontable.InstructionTableModQ;
import seclogin.math.Mod;

import java.util.Arrays;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/** The entry point to SecLogin. */
public class SecLogin {

    private static final Logger log = LoggerFactory.getLogger(SecLogin.class);

    private final UserInterface userInterface;
    private final UserStatePersistence userStatePersistence;
    private final QuestionBank questionBank;
    private final HistoryFileCipher historyFileCipher;
    private final MeasurementParams[] measurementParams;
    private final HistoryFileParams historyFileParams;
    private final InstructionTableModQ instructionTableModQ;
    private final Authenticator authenticator;


    public SecLogin(UserInterface userInterface, UserStatePersistence userStatePersistence,
                    Random random, QuestionBank questionBank, int historyFileSize, Mod q) {
        this.userInterface = userInterface;
        this.userStatePersistence = userStatePersistence;

        this.questionBank = questionBank;
        checkArgument(questionBank.nrOfQuestions() > 0);
        measurementParams = questionBank.measurementParams();
        int nrOfFeatures = measurementParams.length;
        checkState(nrOfFeatures == questionBank.nrOfQuestions());

        historyFileParams = new HistoryFileParams(historyFileSize, nrOfFeatures);

        BlockCipher cipher = new Aes128Cbc();
        historyFileCipher = new HistoryFileCipher(cipher);

        instructionTableModQ = new InstructionTableModQ(q, random);
        authenticator = new Authenticator(instructionTableModQ, measurementParams, historyFileCipher);
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
            log.debug("Writing new instruction table and updated history file");
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
        log.debug("Collected measurements = {}", Arrays.toString(measurements));
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

        log.debug("Adding user `{}'", user);
        UserState userState = generateNewUserState(new User(user), password);

        log.debug("Writing new instruction table and empty history file");
        userStatePersistence.write(userState);
    }

    private UserState generateNewUserState(User user, Password pwd) {
        HardenedPassword hpwd = instructionTableModQ.generateHardenedPassword();
        log.debug("Generated hardened password hpwd = {}", hpwd);

        log.debug("Generating instruction table with no distinguishing features");
        InstructionTable table = instructionTableModQ.generate(
                hpwd, pwd, new Distinguishment[measurementParams.length]);

        log.debug("Creating empty history file of size = {}", historyFileParams.maxNrOfMeasurements);
        HistoryFile emptyHistoryFile = HistoryFile.emptyHistoryFile(user, historyFileParams);

        log.debug("Encrypting history file with hpwd");
        EncryptedHistoryFile encryptedHistoryFile = historyFileCipher.encrypt(emptyHistoryFile, hpwd);

        return new UserState(user, table, encryptedHistoryFile);
    }

}
