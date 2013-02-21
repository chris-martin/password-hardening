package seclogin;

import java.math.BigInteger;
import java.util.Random;

import static com.google.common.base.Preconditions.checkState;

/** Authenticates users against their hardened passwords. */
public class Authenticator {
    
    private final Random random;
    private final MeasurementParams[] measurementParams;
    private final HistoryFileParams historyFileParams;

    public Authenticator(Random random, MeasurementParams[] measurementParams, HistoryFileParams historyFileParams) {
        this.random = random;
        this.measurementParams = measurementParams;
        this.historyFileParams = historyFileParams;
    }

    /**
     * Authenticates the user by recovering their hardened password from their instruction table
     * and history file using the given regular password and measurements (responses to questions).
      */
    public UserState authenticate(UserState userState, String password, double[] measurements) {
        checkState(measurements.length == measurementParams.length);
        
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

    /**
     * Decrypts the user's history file using the given hardened password. Returns null if
     * the password is incorrect or if the decrypted history file is not valid for the user.
     */
    private HistoryFile decryptHistoryFile(UserState userState, BigInteger hpwd)
            throws IndecipherableHistoryFileException {
        HistoryFile historyFile = userState.historyFile.decrypt(hpwd, historyFileParams);
        if (!historyFile.userHashEquals(userState.user)) {
            throw new IndecipherableHistoryFileException();
        }
        return historyFile;
    }
}
