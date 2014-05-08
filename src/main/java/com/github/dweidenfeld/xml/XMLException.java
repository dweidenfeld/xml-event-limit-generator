package com.github.dweidenfeld.xml;

/**
 * General exception for xml handling.
 */
@SuppressWarnings("UnusedDeclaration")
public class XMLException extends RuntimeException {

    public XMLException() {
        super();
    }

    public XMLException(String message) {
        super(message);
    }

    public XMLException(String message, Throwable cause) {
        super(message, cause);
    }

    public XMLException(Throwable cause) {
        super(cause);
    }

    protected XMLException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
