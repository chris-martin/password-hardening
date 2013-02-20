package seclogin;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkState;
import static seclogin.Feature.ALPHA;
import static seclogin.Feature.BETA;

public class Authenticator {
    
    private final Random random;
    private final QuestionBank questionBank;

    public Authenticator(Random random, QuestionBank questionBank) {
        this.random = random;
        this.questionBank = questionBank;
    }

    public UserState authenticate(UserState userState, Password password, double[] measurements) {
        checkState(measurements.length == Parameters.M);
        
        BigInteger hpwd = userState.instructionTable.interpolateHpwd(password, features(measurements));
        HistoryFile historyFile;
        try {
            historyFile = decryptHistoryFile(userState, hpwd);
        } catch (IndecipherableHistoryFileException e) {
            return null;
        }

        historyFile = historyFile.withMostRecentMeasurements(measurements);

        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
                InstructionTable.generate(historyFile.deriveFeatures(questionBank), password, random);

        return new UserState(userState.user, tableAndHpwd.table, historyFile.encrypt(tableAndHpwd.hpwd));
    }
    
    private Feature[] features(double[] measurements) {
        List<Question> questions = questionBank.getQuestions();
        Feature[] features = new Feature[questions.size()];
        for (int i = 0; i < features.length; i++) {
            features[i] = measurements[i] < questions.get(i).getResponseMean() ? ALPHA : BETA;
        }
        return features;
    }

    private HistoryFile decryptHistoryFile(UserState userState, BigInteger hpwd)
            throws IndecipherableHistoryFileException {
        HistoryFile historyFile = userState.historyFile.decrypt(hpwd);
        if (!historyFile.userHashEquals(userState.user)) {
            throw new IndecipherableHistoryFileException();
        }
        return historyFile;
    }
}
