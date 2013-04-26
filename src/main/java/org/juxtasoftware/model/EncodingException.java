package org.juxtasoftware.model;

public class EncodingException extends Exception {
    private static final long serialVersionUID = -2323242497463165307L;

    public EncodingException() {
        super();
    }

    public EncodingException(String message) {
        super(message);
    }

    public EncodingException(String message, Throwable cause) {
        super(message, cause);
    }

    public EncodingException(Throwable cause) {
        super(cause);
    }

}
