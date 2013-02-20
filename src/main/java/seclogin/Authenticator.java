package seclogin;

import com.google.common.collect.Lists;

import java.math.BigInteger;
import java.util.Collections;
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
        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
            InstructionTable.generate(featureValues, password, random);

        return new UserState(userState.user, tableAndHpwd.table, historyFile.encrypt(tableAndHpwd.hpwd));
    }
    
    private List<FeatureValue> features(double[] measurements) {
        checkArgument(measurements.length == measurementParams.size());

        List<FeatureValue> featureValues = Lists.newArrayListWithCapacity(measurements.length);
        for (int i = 0; i < measurements.length; i++) {
            featureValues.add(measurements[i] < measurementParams.get(i).responseMean() ? ALPHA : BETA);
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
        if (stats == null) {
            return allNonDistinguishingFeatureValues();
        }

        checkState(stats.size() == historyFileParams.nrOfFeatures());

        List<FeatureValue> featureValues = Lists.newArrayListWithCapacity(stats.size());
        for (int i = 0; i < stats.size(); i++) {
            MeasurementStats user = stats.get(i);
            MeasurementParams system = measurementParams.get(i);
            boolean isDistinguishing = historyFile.isFull() &&
                    Math.abs(user.mean() - system.responseMean()) > (user.stDev() * system.stDevMultiplier());
            featureValues.add(isDistinguishing ? (user.mean() < system.responseMean() ? ALPHA : BETA) : null);
        }
        return featureValues;
    }

    private List<FeatureValue> allNonDistinguishingFeatureValues() {
        return Collections.nCopies(historyFileParams.nrOfFeatures(), (FeatureValue) null);
    }
}
