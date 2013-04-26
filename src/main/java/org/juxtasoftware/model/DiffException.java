package org.juxtasoftware.model;

public class DiffException extends Exception {
    private static final long serialVersionUID = -1091516078268499964L;

    public DiffException() {
        super();
    }

    public DiffException(String message) {
        super(message);
    }

    public DiffException(String message, Throwable cause) {
        super(message, cause);
    }

    public DiffException(Throwable cause) {
        super(cause);
    }
}
