package rubis.utilities;

/**
 * RequestForPaymentTemplateNotFoundException is the exception raised when the template for generating PDF files could
 * not be found
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class RequestForPaymentTemplateNotFoundException extends Exception {

    /**
     * RequestForPaymentTemplateNotFoundException default constructor
     */
    public RequestForPaymentTemplateNotFoundException() {

        // Initialise parent class
        super("The template file required to generate PDF documents for requests for payment was not found.");
    }

    /**
     * RequestForPaymentTemplateNotFoundException constructor
     * 
     * @param templatePath Path at which the template file was expected to be found
     */
    public RequestForPaymentTemplateNotFoundException(String templatePath) {

        // Initialise parent class
        super("The template file required to generate PDF documents for requests for payment could not be found at "
                + templatePath + ".");
    }
}
