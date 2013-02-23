package seclogin;

import seclogin.historyfile.EncryptedHistoryFile;

/** A user's instruction table and encrypted history file. */
public class UserState {

    public final User user;
    public final InstructionTable instructionTable;
    public final EncryptedHistoryFile encryptedHistoryFile;

    public UserState(User user, InstructionTable instructionTable,
                     EncryptedHistoryFile encryptedHistoryFile) {

        this.user = user;
        this.instructionTable = instructionTable;
        this.encryptedHistoryFile = encryptedHistoryFile;
    }

}
