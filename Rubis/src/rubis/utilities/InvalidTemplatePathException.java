package rubis.utilities;

/**
 * InvalidTemplatePathException is the exception raised when the template path does not appear to be valid
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class InvalidTemplatePathException extends Exception {

    /**
     * InvalidTemplatePathException default constructor
     */
    public InvalidTemplatePathException() {

        // Initialise parent class
        super("The template path does not appear to be valid.");
    }

    /**
     * TemplateNotFoundException constructor
     * 
     * @param templatePath Path at which the template file was expected to be found
     */
    public InvalidTemplatePathException(String templatePath) {

        // Initialise parent class
        super("The template path " + templatePath + " does not appear to be valid.");
    }
}
