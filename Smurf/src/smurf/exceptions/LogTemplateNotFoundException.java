package smurf.exceptions;

/**
 * LogTemplateNotFoundException is the exception raised when the template file for generating request for payment
 * document sending log could not be found
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class LogTemplateNotFoundException extends Exception {

    /**
     * LogTemplateNotFoundException default constructor
     */
    public LogTemplateNotFoundException() {

        // Initialise parent class
        super("The template file required to generate log files for the sending of request for payment documents "
                + "could not be found.");
    }

    /**
     * LogTemplateNotFoundException constructor
     * 
     * @param templatePath Path at which the template file was expected to be found
     */
    public LogTemplateNotFoundException(String templatePath) {

        // Initialise parent class
        super("The template file required to generate log files for the sending of request for payment documents "
                + "could not be found at " + templatePath + ".");
    }
}
