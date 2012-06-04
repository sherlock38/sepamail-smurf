package rubis.utilities;

import java.text.ParseException;

/**
 * DateParseException is the exception raised date parse errors occur
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class DateParseException extends Exception {

    /**
     * DateParseException constructor
     * 
     * @param ex ParseException that was initially raised
     */
    public DateParseException(ParseException ex) {

        // Initialise parent class
        super(ex.getMessage());
    }
}
