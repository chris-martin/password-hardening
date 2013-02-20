package seclogin;

import com.google.common.collect.Lists;

import java.math.BigInteger;
import java.util.List;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static seclogin.FeatureValue.ALPHA;
import static seclogin.FeatureValue.BETA;

public class Authenticator {
    
    private final Random random;
    private final List<MeasurementParams> measurementParams;
    private final HistoryFileParams historyFileParams;

    public Authenticator(Random random, List<MeasurementParams> measurementParams, HistoryFileParams historyFileParams) {
        this.random = random;
        this.measurementParams = measurementParams;
        this.historyFileParams = historyFileParams;
    }

    public UserState authenticate(UserState userState, Password password, double[] measurements) {
        checkState(measurements.length == historyFileParams.nrOfFeatures());
        
        BigInteger hpwd = userState.instructionTable.interpolateHpwd(password, features(measurements));
        HistoryFile historyFile;
        try {
            historyFile = decryptHistoryFile(userState, hpwd);
        } catch (IndecipherableHistoryFileException e) {
            return null;
        }

        historyFile = historyFile.withMostRecentMeasurements(measurements);

        List<FeatureValue> featureValues = deriveFeatures(historyFile);
        FeatureValue[] featuresArray = featureValues.toArray(new FeatureValue[featureValues.size()]);
        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
            InstructionTable.generate(featuresArray, password, random);

        return new UserState(userState.user, tableAndHpwd.table, historyFile.encrypt(tableAndHpwd.hpwd));
    }
    
    private FeatureValue[] features(double[] measurements) {
        FeatureValue[] featureValues = new FeatureValue[measurementParams.size()];
        for (int i = 0; i < featureValues.length; i++) {
            featureValues[i] = measurements[i] < measurementParams.get(i).t() ? ALPHA : BETA;
        }
        return featureValues;
    }

    private HistoryFile decryptHistoryFile(UserState userState, BigInteger hpwd)
            throws IndecipherableHistoryFileException {
        HistoryFile historyFile = userState.historyFile.decrypt(hpwd, historyFileParams);
        if (!historyFile.userHashEquals(userState.user)) {
            throw new IndecipherableHistoryFileException();
        }
        return historyFile;
    }

    List<FeatureValue> deriveFeatures(HistoryFile historyFile) {
        checkArgument(measurementParams.size() == historyFileParams.nrOfFeatures());

        List<MeasurementStats> stats = historyFile.calculateStats();
        checkState(stats.size() == historyFileParams.nrOfFeatures());

        List<FeatureValue> featureValues = Lists.newArrayListWithCapacity(stats.size());
        for (int i = 0; i < stats.size(); i++) {
            MeasurementStats userStats = stats.get(i);
            double mu = userStats.mu();
            double sigma = userStats.sigma();
            double t = measurementParams.get(i).t();
            double k = measurementParams.get(i).k();
            boolean isDistinguishing = historyFile.isFull() && Math.abs(mu - t) > (k * sigma);
            featureValues.add(isDistinguishing ? (mu < t ? ALPHA : BETA) : null);
        }
        return featureValues;
    }
}
