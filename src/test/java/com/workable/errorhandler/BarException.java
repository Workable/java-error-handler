package com.workable.errorhandler;

/**
 * A runtime test exception for using in {@link ErrorHandler} unit tests
 *
 * @author Stratos Pavlakis
 */
public class BarException extends RuntimeException {

    private boolean openBar = true;

    public BarException(String message) {
        super(message);
    }

    public BarException(String message, boolean openBar) {
        super(message);
        this.openBar = openBar;
    }

    public boolean isOpenBar() {
        return openBar;
    }
}
