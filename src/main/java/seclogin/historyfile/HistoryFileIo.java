package seclogin.historyfile;

import com.google.common.io.ByteStreams;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * History file I/O.
 */
public class HistoryFileIo {

    /** Writes the given encrypted history file to the given stream. Closes the stream when done. */
    public void write(EncryptedHistoryFile encryptedHistoryFile, OutputStream outputStream) throws IOException {
        BufferedOutputStream out = new BufferedOutputStream(outputStream);
        out.write(encryptedHistoryFile.ciphertext);
        out.flush();
        out.close();
    }

    /** Reads the encrypted history file supplied by the given stream. Closes the stream when done. */
    public EncryptedHistoryFile read(InputStream inputStream) throws IOException {
        BufferedInputStream in = new BufferedInputStream(inputStream);
        byte[] ciphertext;
        try {
            ciphertext = ByteStreams.toByteArray(in);
        } finally {
            in.close();
        }
        return new EncryptedHistoryFile(ciphertext);
    }
}
