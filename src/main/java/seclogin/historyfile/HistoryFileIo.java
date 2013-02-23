package seclogin.historyfile;

import com.google.common.io.ByteStreams;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * History file I/O.
 */
public class HistoryFileIo {

    /** Writes the given encrypted history file to the given stream. Closes the stream when done. */
    public void write(EncryptedHistoryFile encryptedHistoryFile, OutputStream outputStream) throws IOException {
        checkNotNull(encryptedHistoryFile);
        checkNotNull(outputStream);

        BufferedOutputStream out = new BufferedOutputStream(outputStream);
        try {
            out.write(encryptedHistoryFile.ciphertext);
            out.flush();
        } finally {
            out.close();
        }
    }

    /** Reads the encrypted history file supplied by the given stream. Closes the stream when done. */
    public EncryptedHistoryFile read(InputStream inputStream) throws IOException {
        checkNotNull(inputStream);

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
