package seclogin;

import seclogin.historyfile.EncryptedHistoryFile;
import seclogin.historyfile.HistoryFile;
import seclogin.historyfile.HistoryFileCipher;
import seclogin.historyfile.IndecipherableHistoryFileException;
import seclogin.instructiontable.InstructionTable;

import java.util.Random;

import static com.google.common.base.Preconditions.checkState;

/** Authenticates users against their hardened passwords. */
public class Authenticator {
    
    private final Random random;
    private final MeasurementParams[] measurementParams;
    private final HistoryFileCipher historyFileCipher;

    public Authenticator(Random random, MeasurementParams[] measurementParams, HistoryFileCipher historyFileCipher) {
        this.random = random;
        this.measurementParams = measurementParams;
        this.historyFileCipher = historyFileCipher;
    }

    /**
     * Authenticates the user by recovering their hardened password from their instruction table
     * and history file using the given regular password and measurements (responses to questions).
      */
    public UserState authenticate(UserState userState, Password password, double[] measurements) {
        checkState(measurements.length == measurementParams.length);
        
        HardenedPassword hpwd = userState.instructionTable.interpolateHpwd(password, measurements, measurementParams);
        HistoryFile historyFile;
        try {
            historyFile = historyFileCipher.decrypt(userState.encryptedHistoryFile, hpwd, userState.user);
        } catch (IndecipherableHistoryFileException e) {
            return null;
        }

        // add the new measurements to the history file
        historyFile = historyFile.withMostRecentMeasurements(measurements);

        // calculate the measurement statistics for this user
        MeasurementStats[] measurementStats = historyFile.calculateStats();

        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
            InstructionTable.generate(password, measurementParams, measurementStats, random);

        EncryptedHistoryFile encryptedHistoryFile = historyFileCipher.encrypt(historyFile, tableAndHpwd.hpwd);
        return new UserState(userState.user, tableAndHpwd.table, encryptedHistoryFile);
    }
}
