package smurf.exceptions;

/**
 * DatesNotSpecifiedException is the exception raised when both the start and end dates for payment requests have not
 * been specified
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class DatesNotSpecifiedException extends Exception {

    /**
     * DatesNotSpecifiedException default constructor
     */
    public DatesNotSpecifiedException() {

        // Initialise parent class
        super("The start and end dates for payment requests have not been specified.");
    }

    /**
     * DatesNotSpecifiedException constructor
     * 
     * @param message Exception message
     */
    public DatesNotSpecifiedException(String message) {

        // Initialise parent class
        super(message);
    }
}
