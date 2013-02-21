package seclogin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class UserState {

    public final String user;
    public final InstructionTable instructionTable;
    public final HistoryFile.Encrypted historyFile;

    public UserState(String user, InstructionTable instructionTable, HistoryFile.Encrypted historyFile) {
        this.user = user;
        this.instructionTable = instructionTable;
        this.historyFile = historyFile;
    }

    public void write(File dir) {
        writeInstructionTable(dir);
        writeHistoryFile(dir);
    }

    private void writeHistoryFile(File dir) {
        try {
            FileOutputStream out = new FileOutputStream(historyFile(dir, user));
            historyFile.write(out);
            out.close();
        } catch (IOException e) {
            System.err.println("Could not write history file.");
            System.exit(1);
        }
    }

    private void writeInstructionTable(File dir) {
        try {
            FileOutputStream out = new FileOutputStream(instructionTableFile(dir, user));
            instructionTable.write(out);
            out.close();
        } catch (IOException e) {
            System.err.println("Could not write instruction table.");
            System.exit(1);
        }
    }

    public static UserState read(File dir, String user, MeasurementParams[] measurementParams) {
        InstructionTable instructionTable;
        try {
            FileInputStream in = new FileInputStream(instructionTableFile(dir, user));
            instructionTable = InstructionTable.read(in, measurementParams);
            in.close();
        } catch (FileNotFoundException e) {
            return null;
        } catch (IOException e) {
            System.err.println("Could not read instruction table.");
            System.exit(1);
            return null;
        }

        HistoryFile.Encrypted encryptedHistoryFile;
        try {
            FileInputStream in = new FileInputStream(historyFile(dir, user));
            encryptedHistoryFile = HistoryFile.read(in);
            in.close();
        } catch (IOException e) {
            System.err.println("Could not read instruction table.");
            System.exit(1);
            return null;
        }

        return new UserState(user, instructionTable, encryptedHistoryFile);
    }

    private static File instructionTableFile(File dir, String user) {
        return new File(dir, "instruction-table-" + user);
    }

    private static File historyFile(File dir, String user) {
        return new File(dir, "history-file-" + user);
    }
}
