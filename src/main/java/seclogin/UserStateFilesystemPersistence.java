package seclogin;

import seclogin.historyfile.EncryptedHistoryFile;
import seclogin.historyfile.HistoryFileIo;
import seclogin.instructiontable.InstructionTable;
import seclogin.instructiontable.InstructionTableIo;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserStateFilesystemPersistence implements UserStatePersistence {

    private final File dir;
    private final HistoryFileIo historyFileIo = new HistoryFileIo();
    private final InstructionTableIo instructionTableIo = new InstructionTableIo();

    public UserStateFilesystemPersistence(File dir) {
        this.dir = dir;
    }

    public void write(UserState userState) {
        writeInstructionTable(userState);
        writeHistoryFile(userState);
    }

    private void writeHistoryFile(UserState userState) {
        try {
            FileOutputStream out = new FileOutputStream(historyFile(userState.user));
            historyFileIo.write(userState.encryptedHistoryFile, out);
        } catch (IOException e) {
            throw new RuntimeException("Could not write history file.", e);
        }
    }

    private void writeInstructionTable(UserState userState) {
        try {
            FileOutputStream out = new FileOutputStream(instructionTableFile(userState.user));
            instructionTableIo.write(userState.instructionTable, out);
        } catch (IOException e) {
            throw new RuntimeException("Could not write instruction table.", e);
        }
    }

    @Nullable
    public UserState read(User user) {
        InstructionTable instructionTable;
        try {
            FileInputStream in = new FileInputStream(instructionTableFile(user));
            instructionTable = instructionTableIo.read(in);
        } catch (FileNotFoundException e) {
            return null; // user doesn't exist
        } catch (IOException e) {
            throw new RuntimeException("Could not read instruction table.");
        }

        EncryptedHistoryFile encryptedHistoryFile;
        try {
            FileInputStream in = new FileInputStream(historyFile(user));
            encryptedHistoryFile = historyFileIo.read(in);
        } catch (IOException e) {
            throw new RuntimeException("Could not read instruction table.", e);
        }

        return new UserState(user, instructionTable, encryptedHistoryFile);
    }

    private File instructionTableFile(User user) {
        return new File(dir, "instruction-table-" + user.user);
    }

    private File historyFile(User user) {
        return new File(dir, "history-file-" + user.user);
    }

}
