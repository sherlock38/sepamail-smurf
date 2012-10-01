package smurf.exceptions;

/**
 * EndDateNotSpecifiedException is the exception raised when the end date for payment requests has not been specified
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class EndDateNotSpecifiedException extends Exception {

    /**
     * EndDateNotSpecifiedException default constructor
     */
    public EndDateNotSpecifiedException() {

        // Initialise parent class
        super("The end date for payment requests has not been specified.");
    }

    /**
     * EndDateNotSpecifiedException constructor
     * 
     * @param message Exception message
     */
    public EndDateNotSpecifiedException(String message) {

        // Initialise parent class
        super(message);
    }
}
