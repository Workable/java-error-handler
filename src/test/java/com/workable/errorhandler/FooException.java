package com.workable.errorhandler;

/**
 * A checked test exception for using in {@link ErrorHandler} unit tests
 *
 * @author Stratos Pavlakis
 */
public class FooException extends Exception {

    private boolean fatal = false;

    public FooException(String message) {
        super(message);
    }

    public FooException(String message, boolean fatal) {
        super(message);
        this.fatal = fatal;
    }

    public boolean isFatal() {
        return fatal;
    }
}
