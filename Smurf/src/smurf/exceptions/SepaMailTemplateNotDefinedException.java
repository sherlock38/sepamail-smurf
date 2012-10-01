package smurf.exceptions;

/**
 * SepaMailTemplateNotDefinedException is the exception raised when the template for generating SEPAmail XML document
 * is not defined
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class SepaMailTemplateNotDefinedException extends Exception {

    /**
     * SepaMailTemplateNotDefinedException default constructor
     */
    public SepaMailTemplateNotDefinedException() {

        // Initialise parent class
        super("The template file required to generate SEPAmail XML document was not found.");
    }
}
