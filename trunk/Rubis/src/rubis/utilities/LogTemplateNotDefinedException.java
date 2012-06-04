package rubis.utilities;

/**
 * LogTemplateNotDefinedException is the exception raised when the template file for generating request for payment
 * document sending log was not defined
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class LogTemplateNotDefinedException extends Exception {

    /**
     * LogTemplateNotDefinedException default constructor
     */
    public LogTemplateNotDefinedException() {

        // Initialise parent class
        super("The template file required to generate log files for the sending of request for payment documents has "
                + "not been defined.");
    }
}
