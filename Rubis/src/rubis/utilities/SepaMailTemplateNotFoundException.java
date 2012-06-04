package rubis.utilities;

/**
 * SepaMailTemplateNotFoundException is the exception raised when the template for generating SEPAmail XML documents
 * could not be found
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class SepaMailTemplateNotFoundException extends Exception {

    /**
     * SepaMailTemplateNotFoundException default constructor
     */
    public SepaMailTemplateNotFoundException() {

        // Initialise parent class
        super("The template file required to generate SEPAmail XML documents was not found.");
    }

    /**
     * SepaMailTemplateNotFoundException constructor
     * 
     * @param templatePath Path at which the template file was expected to be found
     */
    public SepaMailTemplateNotFoundException(String templatePath) {

        // Initialise parent class
        super("The template file required to generate SEPAmail XML documents could not be found at " + templatePath
                + ".");
    }
}
