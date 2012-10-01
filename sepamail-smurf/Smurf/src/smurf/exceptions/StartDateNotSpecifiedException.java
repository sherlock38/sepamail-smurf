package smurf.exceptions;

/**
 * StartDateNotSpecifiedException is the exception raised when the start date for payment requests has not been
 * specified
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class StartDateNotSpecifiedException extends Exception {

    /**
     * StartDateNotSpecifiedException default constructor
     */
    public StartDateNotSpecifiedException() {

        // Initialise parent class
        super("The start date for payment requests has not been specified.");
    }

    /**
     * StartDateNotSpecifiedException constructor
     * 
     * @param message Exception message
     */
    public StartDateNotSpecifiedException(String message) {

        // Initialise parent class
        super(message);
    }
}
