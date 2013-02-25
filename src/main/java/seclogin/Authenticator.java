package seclogin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import seclogin.historyfile.EncryptedHistoryFile;
import seclogin.historyfile.HistoryFile;
import seclogin.historyfile.HistoryFileCipher;
import seclogin.historyfile.IndecipherableHistoryFileException;
import seclogin.instructiontable.Distinguishment;
import seclogin.instructiontable.DistinguishmentPolicy;
import seclogin.instructiontable.InstructionTable;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Random;

/** Authenticates users against their hardened passwords. */
public class Authenticator {

    private static final Logger log = LoggerFactory.getLogger(Authenticator.class);
    
    private final Random random;
    private final HistoryFileCipher historyFileCipher;
    private final DistinguishmentPolicy distinguishmentPolicy;


    public Authenticator(
            Random random,
            MeasurementParams[] measurementParams,
            HistoryFileCipher historyFileCipher) {

        this.random = random;
        this.historyFileCipher = historyFileCipher;
        distinguishmentPolicy =
                new DistinguishmentPolicy(measurementParams);
    }

    /**
     * Authenticates the user by recovering their hardened password from their instruction table
     * and history file using the given regular password and measurements (responses to questions).
      */
    @Nullable
    public UserState authenticate(UserState userState, Password password, double[] measurements) {
        log.debug("Authenticating user `{}' with measurements = {}", userState.user, Arrays.toString(measurements));

        // determine feature distinguishment from measurements
        Distinguishment[] distinguishments =
                distinguishmentPolicy.measurementDistinguishment(measurements);
        log.debug("Determined (from measurements) feature distinguishments = {}", Arrays.toString(distinguishments));

        // interpolate hardened password from regular password and distinguishments
        HardenedPassword hpwd = userState.instructionTable.interpolateHpwd(password, distinguishments);
        log.debug("Interpolated hpwd = {}", hpwd);

        // try to decrypt the history file with the recovered hardened password
        HistoryFile historyFile;
        try {
            historyFile = historyFileCipher.decrypt(userState.encryptedHistoryFile, hpwd, userState.user);
            log.debug("Decrypted history file successfully");
        } catch (IndecipherableHistoryFileException e) {
            log.debug("Could not decrypt history file => hpwd incorrect");
            // the hardened password is incorrect
            return null;
        }

        log.debug("Adding new measurements to history file");
        historyFile = historyFile.withMostRecentMeasurements(measurements);

        // calculate the historical measurement statistics for this user
        MeasurementStats[] measurementStats = historyFile.calculateStats();
        log.debug("Calculated historical measurement statistics = {}", Arrays.toString(measurementStats));

        // determine feature distinguishment from user's statistics
        distinguishments = distinguishmentPolicy.userDistinguishment(measurementStats);
        log.debug("Determined (from measurement statistics) feature distinguishments = {}",
                Arrays.toString(distinguishments));

        log.debug("Generating from distinguishments new instruction table and hardened password");
        InstructionTable.InstructionTableAndHardenedPassword tableAndHpwd =
            InstructionTable.generate(password, distinguishments, random);

        log.debug("Encrypting history file");
        EncryptedHistoryFile encryptedHistoryFile = historyFileCipher.encrypt(historyFile, tableAndHpwd.hpwd);

        return new UserState(userState.user, tableAndHpwd.table, encryptedHistoryFile);
    }
}
