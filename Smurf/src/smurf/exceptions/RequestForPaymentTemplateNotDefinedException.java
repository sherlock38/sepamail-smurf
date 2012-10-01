package smurf.exceptions;

/**
 * RequestForPaymentTemplateNotDefinedException is the exception raised when the template for generating PDF request for
 * payment files has not been defined
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class RequestForPaymentTemplateNotDefinedException extends Exception {

    /**
     * RequestForPaymentTemplateNotDefinedException default constructor
     */
    public RequestForPaymentTemplateNotDefinedException() {

        // Initialise parent class
        super("The template file required to generate PDF requests for payment documents was not found.");
    }
}
