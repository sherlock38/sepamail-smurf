package smurf.model;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import smurf.exceptions.RequestForPaymentAttributeNotFoundException;

/**
 * The AvisClient object represent a row in the 'avis' table merged with the corresponding row in the 'client' table -
 * it contains details about request for payments
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 0.8
 */
public class AvisClient {

    private BigInteger idAvis;
    private boolean generateDocument;
    private HashMap<String, Object> attributes;
    private NumberFormat numberFormatter;
    private SimpleDateFormat dateFormat;
    private String pdfFile;

    /**
     * Get the value of an attribute
     * 
     * @param key Name of the attribute for which the value needs to be found
     * @return Value of the attribute having the specified key name
     * @throws RequestForPaymentAttributeNotFoundException
     */
    public Object getAttribute(String key) throws RequestForPaymentAttributeNotFoundException {

        // Check if the given key does not exist in the attributes map
        if (!this.attributes.containsKey(key)) {
            throw new RequestForPaymentAttributeNotFoundException(key);
        }

        // Attribute value for the given key
        return this.attributes.get(key);
    }

    /**
     * Set the value of an attribute
     * 
     * @param key Name of the attribute
     * @param value Value of the attribute
     */
    public void setAttribute(String key, Object value) {
        this.attributes.put(key, value);
    }

    /**
     * Get the list of attributes for the current request for payment
     * 
     * @return List of attributes for the current request for payment
     */
    public HashMap<String, Object> getAttributes()
    {
        return this.attributes;
    }

    /**
     * Set the list of attributes for the current request for payment
     * 
     * @param attributes List of attributes for the current request for payment
     */
    public void setAttributes(HashMap<String, Object> attributes)
    {
        this.attributes = attributes;
    }

    /**
     * Get formatted attribute value for the specified key
     * 
     * @param key Name of the attribute
     * @return Formatted attribute value
     * @throws RequestForPaymentAttributeNotFoundException
     */
    public String getFormattedAttribute(String key) throws RequestForPaymentAttributeNotFoundException {

        // Check if the given key does not exist in the attributes map
        if (!this.attributes.containsKey(key)) {
            throw new RequestForPaymentAttributeNotFoundException(key);
        }

        // Check the attribute value type
        if (this.attributes.get(key).getClass() == String.class) {
            return this.attributes.get(key).toString();
        } else if (this.attributes.get(key).getClass() == Date.class) {
            return this.dateFormat.format((Date)this.attributes.get(key));
        } else if (this.attributes.get(key).getClass() == BigDecimal.class) {
            return this.numberFormatter.format((BigDecimal)this.attributes.get(key));
        } else if ((this.attributes.get(key).getClass() == double.class)
                || (this.attributes.get(key).getClass() == Double.class)) {
            return this.numberFormatter.format((Double)this.attributes.get(key));
        } else if ((this.attributes.get(key).getClass() == int.class)
                || (this.attributes.get(key).getClass() == Integer.class)) {
            return this.attributes.get(key).toString();
        }

        // Attribute value for the given key if type could not be determined
        return this.attributes.get(key).toString();
    }

    /**
     * Get the generate document status of the current AvisClient instance
     * 
     * @return Generate document status of the current AvisClient instance
     */
    public boolean getGenerateDocument() {
        return this.generateDocument;
    }

    /**
     * Set the generate document status of the current AvisClient instance
     * 
     * @param generateDocument Generate document status of the current AvisClient instance
     */
    public void setGenerateDocument(boolean generateDocument) {
        this.generateDocument = generateDocument;
    }

    /**
     * Get the unique row ID of the request for payment
     * 
     * @return Unique row ID of the request for payment
     */
    public BigInteger getIdAvis() {
        return idAvis;
    }

    /**
     * Set the unique row ID of the request for payment
     * 
     * @param idAvis Unique row ID of the request for payment
     */
    public void setIdAvis(BigInteger idAvis) {
        this.idAvis = idAvis;
    }

    /**
     * Get the path and filename of the generated PDF file
     * 
     * @return Path and filename of the generated PDF file
     */
    public String getPdfFile() {
        return this.pdfFile;
    }

    /**
     * Set the path and filename of the generated PDF file
     * 
     * @param pdfFile Path and filename of the generated PDF file
     */
    public void setPdfFile(String pdfFile) {
        this.pdfFile = pdfFile;
    }

    /**
     * AvisClient constructor
     * 
     * @param idAvis Unique row ID of the request for payment
     */
    public AvisClient(BigInteger idAvis) {

        // Initialise the attributes of the class
        this.attributes = new HashMap<String, Object>();
        this.dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        this.generateDocument = true;
        this.idAvis = idAvis;
        this.numberFormatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        this.pdfFile = null;
    }
}
