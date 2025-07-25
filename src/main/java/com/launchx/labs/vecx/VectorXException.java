package com.launchx.labs.vecx;

/**
 * Exception class for VectorX operations.
 * This exception is thrown when VectorX operations fail, providing detailed error information.
 */
public class VectorXException extends Exception {
    
    private final int errorCode;
    
    /**
     * Constructs a new VectorXException with the specified detail message.
     * 
     * @param message the detail message
     */
    public VectorXException(String message) {
        super(message);
        this.errorCode = -1;
    }
    
    /**
     * Constructs a new VectorXException with the specified detail message and cause.
     * 
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public VectorXException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = -1;
    }
    
    /**
     * Constructs a new VectorXException with the specified detail message and error code.
     * 
     * @param message the detail message
     * @param errorCode the native error code
     */
    public VectorXException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * Constructs a new VectorXException with the specified detail message, error code, and cause.
     * 
     * @param message the detail message
     * @param errorCode the native error code
     * @param cause the cause of the exception
     */
    public VectorXException(String message, int errorCode, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * Gets the native error code associated with this exception.
     * 
     * @return the error code, or -1 if no specific error code is available
     */
    public int getErrorCode() {
        return errorCode;
    }
    
    /**
     * Returns a string representation of this exception including the error code.
     * 
     * @return a string representation of this exception
     */
    @Override
    public String toString() {
        if (errorCode != -1) {
            return super.toString() + " (Error Code: " + errorCode + ")";
        }
        return super.toString();
    }
}