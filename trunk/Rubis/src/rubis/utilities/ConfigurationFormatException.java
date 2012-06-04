package rubis.utilities;

/**
 * ConfigurationFormatException is the exception raised when a non-matching data type has been defined for an
 * application configuration
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class ConfigurationFormatException extends Exception {

    /**
     * ConfigurationFormatException default constructor
     */
    public ConfigurationFormatException() {

        // Initialise parent class
        super();
    }

    /**
     * ConfigurationFormatException constructor
     * 
     * @param message Exception message
     */
    public ConfigurationFormatException(String message) {

        // Initialise parent class
        super(message);
    }
}
