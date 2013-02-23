package seclogin.historyfile;

/** Thrown to indicate the history file cannot be decrypted properly or is otherwise invalid. */
public class IndecipherableHistoryFileException extends Exception {

    public IndecipherableHistoryFileException() {
        super();
    }

    public IndecipherableHistoryFileException(Throwable cause) {
        super(cause);
    }

}
