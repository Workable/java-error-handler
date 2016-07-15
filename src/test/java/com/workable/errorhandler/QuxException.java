package com.workable.errorhandler;

/**
 * A checked test exception for using in {@link ErrorHandler} unit tests
 *
 * @author Stratos Pavlakis
 */
public class QuxException extends Exception {

    private int errorStatus = 0;

    public QuxException(int errorStatus) {
        this.errorStatus = errorStatus;
    }

    public int getErrorStatus() {
        return errorStatus;
    }
}
