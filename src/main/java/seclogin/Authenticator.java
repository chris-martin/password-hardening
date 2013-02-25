package seclogin;

import seclogin.historyfile.EncryptedHistoryFile;
import seclogin.historyfile.HistoryFile;
import seclogin.historyfile.HistoryFileCipher;
import seclogin.historyfile.IndecipherableHistoryFileException;
import seclogin.instructiontable.Distinguishment;
import seclogin.instructiontable.DistinguishmentPolicy;
import seclogin.instructiontable.InstructionTable;

import javax.annotation.Nullable;
import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;

/** Authenticates users against their hardened passwords. */
public class Authenticator {
    
    private final Random random;
    private final MeasurementParams[] measurementParams;
    private final HistoryFileCipher historyFileCipher;
    private final DistinguishmentPolicy distinguishmentPolicy = new DistinguishmentPolicy();

    public Authenticator(Random random, MeasurementParams[] measurementParams, HistoryFileCipher historyFileCipher) {
        this.random = random;
        this.measurementParams = measurementParams;
        this.historyFileCipher = historyFileCipher;
    }

    /**
     * Authenticates the user by recovering their hardened password from their instruction table
     * and history file using the given regular password and measurements (responses to questions).
      */
    @Nullable
    public UserState authenticate(UserState userState, Password password, double[] measurements) {
        checkArgument(measurements.length == measurementParams.length);

        // determine feature distinguishment from measurements
        Distinguishment[] distinguishments =
                distinguishmentPolicy.measurmentDistinguishment(measurements, measurementParams);

        // interpolate hardened password from regular password and distinguishments
        HardenedPassword hpwd = userState.instructionTable.interpolateHpwd(password, distinguishments);

        // try to decrypt the history file with the recovered hardened password
        HistoryFile historyFile;
        try {
            historyFile = historyFileCipher.decrypt(userState.encryptedHistoryFile, hpwd, userState.user);
        } catch (IndecipherableHistoryFileException e) {
            // the hardened password is incorrect
            return null;
        }

        // add the new measurements to the history file
        historyFile = historyFile.withMostRecentMeasurements(measurements);

        // calculate the historical measurement statistics for this user
        MeasurementStats[] measurementStats = historyFile.calculateStats();

        // determine feature distinguishment from user's statistics
        distinguishments = distinguishmentPolicy.userDistinguishment(measurementStats, measurementParams);

        // create new instruction table and hardened password
        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
            InstructionTable.generate(password, distinguishments, random);

        // encrypt and write updated history file
        EncryptedHistoryFile encryptedHistoryFile = historyFileCipher.encrypt(historyFile, tableAndHpwd.hpwd);
        return new UserState(userState.user, tableAndHpwd.table, encryptedHistoryFile);
    }
}
