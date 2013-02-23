package seclogin.crypto;

/** Thrown to indicate ciphertext that cannot be decrypted properly. */
public class IndecipherableException extends Exception {

    public IndecipherableException(Throwable cause) {
        super(cause);
    }
}
