package rubis.utilities;

/**
 * RequestForPaymentAttributeNotFoundException is the exception raised when the attribute requested cannot be found in
 * the list of attributes for a request for payment
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class RequestForPaymentAttributeNotFoundException extends Exception {

    /**
     * RequestForPaymentAttributeNotFoundException default constructor
     */
    public RequestForPaymentAttributeNotFoundException() {

        // Initialise parent class
        super("The attribute requested could not be found in the list of attributes of the request for payment.");
    }

    /**
     * RequestForPaymentAttributeNotFoundException constructor
     * 
     * @param name Name of the attribute requested
     */
    public RequestForPaymentAttributeNotFoundException(String name) {

        // Initialise parent class
        super("The attribute " + name + " could not be found in the list of attributes of the request for payment.");
    }
}
