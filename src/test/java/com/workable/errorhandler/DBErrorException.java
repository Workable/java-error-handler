package com.workable.errorhandler;

/**
 * A runtime test exception for using in {@link ErrorHandler} unit tests
 *
 * @author Pavlos-Petros Tournaris
 */
public class DBErrorException extends Exception {

    private boolean isDBError;

    public DBErrorException(String message, boolean isDBError) {
        super(message);
        this.isDBError = isDBError;
    }

    public boolean isDBError() {
        return isDBError;
    }
}
