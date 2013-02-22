package seclogin;

import java.io.*;

public class UserStateFilesystemPersistence implements UserStatePersistence {

    /** The directory in which to store user history files and instruction tables. */
    private File userStateDir() {
        File file = new File(".seclogin");
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    public void write(UserState userState) {
        writeInstructionTable(userState);
        writeHistoryFile(userState);
    }

    private void writeHistoryFile(UserState userState) {
        try {
            FileOutputStream out = new FileOutputStream(historyFile(userState.user));
            userState.historyFile.write(out);
            out.close();
        } catch (IOException e) {
            System.err.println("Could not write history file.");
            System.exit(1);
        }
    }

    private void writeInstructionTable(UserState userState) {
        try {
            FileOutputStream out = new FileOutputStream(instructionTableFile(userState.user));
            userState.instructionTable.write(out);
            out.close();
        } catch (IOException e) {
            System.err.println("Could not write instruction table.");
            System.exit(1);
        }
    }

    public UserState read(String user, MeasurementParams[] measurementParams) {
        InstructionTable instructionTable;
        try {
            FileInputStream in = new FileInputStream(instructionTableFile(user));
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
            FileInputStream in = new FileInputStream(historyFile(user));
            encryptedHistoryFile = HistoryFile.read(in);
            in.close();
        } catch (IOException e) {
            System.err.println("Could not read instruction table.");
            System.exit(1);
            return null;
        }

        return new UserState(user, instructionTable, encryptedHistoryFile);
    }

    private File instructionTableFile(String user) {
        return new File(userStateDir(), "instruction-table-" + user);
    }

    private File historyFile(String user) {
        return new File(userStateDir(), "history-file-" + user);
    }

}
