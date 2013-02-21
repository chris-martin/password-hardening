package seclogin;

import java.math.BigInteger;
import java.util.Random;

import static com.google.common.base.Preconditions.checkState;

public class Authenticator {
    
    private final Random random;
    private final MeasurementParams[] measurementParams;
    private final HistoryFileParams historyFileParams;

    public Authenticator(Random random, MeasurementParams[] measurementParams, HistoryFileParams historyFileParams) {
        this.random = random;
        this.measurementParams = measurementParams;
        this.historyFileParams = historyFileParams;
    }

    public UserState authenticate(UserState userState, String password, double[] measurements) {
        checkState(measurements.length == historyFileParams.nrOfFeatures());
        
        BigInteger hpwd = userState.instructionTable.interpolateHpwd(password, measurements);
        HistoryFile historyFile;
        try {
            historyFile = decryptHistoryFile(userState, hpwd);
        } catch (IndecipherableHistoryFileException e) {
            return null;
        }

        historyFile = historyFile.withMostRecentMeasurements(measurements);

        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
            InstructionTable.generate(password, measurementParams, historyFile.calculateStats(), random);

        return new UserState(userState.user, tableAndHpwd.table, historyFile.encrypt(tableAndHpwd.hpwd));
    }

    private HistoryFile decryptHistoryFile(UserState userState, BigInteger hpwd)
            throws IndecipherableHistoryFileException {
        HistoryFile historyFile = userState.historyFile.decrypt(hpwd, historyFileParams);
        if (!historyFile.userHashEquals(userState.user)) {
            throw new IndecipherableHistoryFileException();
        }
        return historyFile;
    }
}
