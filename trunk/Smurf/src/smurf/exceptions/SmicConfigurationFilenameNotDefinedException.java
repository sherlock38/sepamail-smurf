package smurf.exceptions;

/**
 * SmicConfigurationFilenameNotDefinedException is the exception raised when the SMIC module configuration filename has
 * not been defined.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class SmicConfigurationFilenameNotDefinedException extends Exception {

    /**
     * SmicConfigurationFilenameNotDefinedException default constructor
     */
    public SmicConfigurationFilenameNotDefinedException() {

        // Initialise parent class
        super("The SMIC module configuration filename has not been defined.");
    }
}
