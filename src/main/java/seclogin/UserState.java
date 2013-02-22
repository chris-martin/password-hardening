package seclogin;

/** A user's instruction table and history file. */
public class UserState {

    public final String user;
    public final InstructionTable instructionTable;
    public final HistoryFile.Encrypted historyFile;

    public UserState(String user, InstructionTable instructionTable,
                     HistoryFile.Encrypted historyFile) {

        this.user = user;
        this.instructionTable = instructionTable;
        this.historyFile = historyFile;
    }

}
