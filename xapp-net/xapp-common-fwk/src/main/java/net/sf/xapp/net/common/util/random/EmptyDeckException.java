package net.sf.xapp.net.common.util.random;

public class EmptyDeckException extends RuntimeException {

    public EmptyDeckException() {
    }

    public EmptyDeckException(String message) {
        super(message);
    }

    public EmptyDeckException(String message, Throwable cause) {
        super(message, cause);
    }

    public EmptyDeckException(Throwable cause) {
        super(cause);
    }
}
