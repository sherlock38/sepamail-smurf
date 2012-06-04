package rubis.utilities;

/**
 * MailParameterNotDefinedException is the exception raised when a parameter required for mailing request for payment
 * documents has not been defined
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class MailParameterNotDefinedException extends Exception {

    /**
     * SepaMailTemplateNotDefinedException constructor
     * 
     * @param paramName Name of the parameter which has not been defined
     */
    public MailParameterNotDefinedException(String paramName) {

        // Initialise parent class
        super("The " + paramName + " required for sending request for payment documents has not been defined.");
    }
}
