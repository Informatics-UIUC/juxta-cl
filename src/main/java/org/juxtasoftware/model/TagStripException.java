package org.juxtasoftware.model;

public class TagStripException extends Exception {
    private static final long serialVersionUID = 3082031421892219435L;
    public TagStripException() {
        super();
    }

    public TagStripException(String message) {
        super(message);
    }

    public TagStripException(String message, Throwable cause) {
        super(message, cause);
    }

    public TagStripException(Throwable cause) {
        super(cause);
    }
}
