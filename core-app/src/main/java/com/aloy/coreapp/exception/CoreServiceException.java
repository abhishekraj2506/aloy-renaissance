package com.aloy.coreapp.exception;

public class CoreServiceException extends RuntimeException {


    /**
     * Instantiates a new Partner source exception.
     */
    public CoreServiceException() {
        super();
    }

    /**
     * Instantiates a new Partner source exception.
     *
     * @param exception the exception
     */
    public CoreServiceException(String exception) {
        super(exception);
    }

    /**
     * Instantiates a new Partner source exception.
     *
     * @param errorMsg  the error msg
     * @param exception the exception
     */
    public CoreServiceException(String errorMsg, Exception exception) {
        super(errorMsg, exception);
    }


}
