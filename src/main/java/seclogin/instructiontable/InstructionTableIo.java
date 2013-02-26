package seclogin.instructiontable;

import seclogin.instructiontable.InstructionTable.Entry;
import seclogin.io.BigIntegerInputStream;
import seclogin.io.BigIntegerOutputStream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Instruction table I/O.
 */
public class InstructionTableIo {

    /** Writes the given instruction table to the given stream. Closes the stream when done. */
    public void write(InstructionTable table, OutputStream outputStream) throws IOException {
        BigIntegerOutputStream out = new BigIntegerOutputStream(new BufferedOutputStream(outputStream));
        try {
            out.writeInt(table.r.length);
            out.write(table.r);

            out.writeInt(table.table.length);
            for (Entry entry : table.table) {
                out.writeBigInteger(entry.alpha);
                out.writeBigInteger(entry.beta);
            }
            out.flush();
        } finally {
            out.close();
        }
    }

    /** Reads the instruction table supplied by the given stream. Closes the stream when done. */
    public InstructionTable read(InputStream inputStream) throws IOException {
        BigIntegerInputStream in = new BigIntegerInputStream(new BufferedInputStream(inputStream));
        try {
            byte[] r = new byte[in.readInt()];
            in.readFully(r);

            Entry[] entries = new Entry[in.readInt()];
            for (int i = 0; i < entries.length; i++) {
                entries[i] = new Entry(in.readBigInteger(), in.readBigInteger());
            }
            return new InstructionTable(r, entries);
        } finally {
            in.close();
        }
    }
}
