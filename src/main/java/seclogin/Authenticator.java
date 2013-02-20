package seclogin;

import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkState;

public class Authenticator {

    private String user;
    private Password password;
    private Feature[] features;

    private InstructionTable instructionTable;
    private HistoryFile.Encrypted encryptedHistoryFile;

    public Authenticator(String user,
                         Password password,
                         Feature[] features,
                         InstructionTable instructionTable,
                         HistoryFile.Encrypted encryptedHistoryFile) {
        this.user = user;
        this.password = password;
        this.features = features;
        this.instructionTable = instructionTable;
        this.encryptedHistoryFile = encryptedHistoryFile;
    }

    public boolean authenticate() {
        checkState(features.length == Parameters.M);
        BigInteger hpwd = instructionTable.interpolateHpwd(password, features);
        HistoryFile historyFile;
        try {
            historyFile = encryptedHistoryFile.decrypt(hpwd);
        } catch (IndecipherableHistoryFileException e) {
            return false;
        }

        // TODO update history file

        return true;
    }

    private HistoryFile decryptHistoryFile(BigInteger hpwd) throws IndecipherableHistoryFileException {
        HistoryFile historyFile = encryptedHistoryFile.decrypt(hpwd);
        if (!historyFile.userHashEquals(user)) {
            throw new IndecipherableHistoryFileException();
        }
        return historyFile;
    }
}
