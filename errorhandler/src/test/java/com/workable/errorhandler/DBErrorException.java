package com.workable.errorhandler;

/**
 * A runtime test exception for using in {@link ErrorHandler} unit tests
 *
 * @author Pavlos-Petros Tournaris
 */
public class DBErrorException extends Exception {

    public DBErrorException(String message) {
        super(message);
    }
}
