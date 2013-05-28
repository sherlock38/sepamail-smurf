package smurf.exceptions;

/**
 * EbicsParameterNotDefinedException is the exception raised when a parameter required for sending documents via eBICS
 * has not been defined
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class EbicsParameterNotDefinedException extends Exception {

    /**
     * EbicsParameterNotDefinedException constructor
     * 
     * @param paramName Name of the parameter which has not been defined
     */
    public EbicsParameterNotDefinedException(String paramName) {

        // Initialise parent class
        super("The " + paramName + " required for sending documents via eBICS has not been defined.");
    }
}
