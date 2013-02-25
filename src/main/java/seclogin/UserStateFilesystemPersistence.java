package seclogin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(UserStateFilesystemPersistence.class);

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
            File file = historyFile(userState.user);
            log.debug("Writing history file for user `{}' to `{}'", userState.user, file);
            FileOutputStream out = new FileOutputStream(file);
            historyFileIo.write(userState.encryptedHistoryFile, out);
        } catch (IOException e) {
            throw new RuntimeException("Could not write history file.", e);
        }
    }

    private void writeInstructionTable(UserState userState) {
        try {
            File file = instructionTableFile(userState.user);
            log.debug("Writing instruction table for user `{}' to `{}'", userState.user, file);
            FileOutputStream out = new FileOutputStream(file);
            instructionTableIo.write(userState.instructionTable, out);
        } catch (IOException e) {
            throw new RuntimeException("Could not write instruction table.", e);
        }
    }

    @Nullable
    public UserState read(User user) {
        InstructionTable instructionTable;
        try {
            File file = instructionTableFile(user);
            log.debug("Reading instruction table for user `{}' from `{}'", user, file);
            FileInputStream in = new FileInputStream(file);
            instructionTable = instructionTableIo.read(in);
        } catch (FileNotFoundException e) {
            log.debug("No instruction table found for user `{}'", user);
            return null; // user doesn't exist
        } catch (IOException e) {
            throw new RuntimeException("Could not read instruction table.");
        }

        EncryptedHistoryFile encryptedHistoryFile;
        try {
            File file = historyFile(user);
            log.debug("Reading history file for user `{}' from `{}'", user, file);
            FileInputStream in = new FileInputStream(file);
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
