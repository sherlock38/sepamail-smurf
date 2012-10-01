package smurf.model;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import smurf.Smurf;
import smurf.exceptions.ConfigurationFormatException;
import smurf.exceptions.DateParseException;

/**
 * The Configuration object represent an application configuration setting that is read from the rubis.properties file
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class Configuration implements Comparable<Configuration> {

    private boolean boolVal;
    private boolean display;
    private boolean isAdvanced;
    private Date dateVal;
    private DecimalFormat decimalFormat;
    private double doubleVal;
    private float floatVal;
    private int intVal;
    private int order;
    private SimpleDateFormat dateFormat;
    private String classTypeName;
    private String propertyName;
    private String stringVal;
    private String title;

    /**
     * Get the boolean value associated with the current instance of the Configuration class
     * 
     * @return Boolean value associated with the current instance of the class
     * @throws ConfigurationFormatException
     */
    public Boolean getBoolVal() throws ConfigurationFormatException {

        // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(boolean.class.getName())) {
            
            // The current instance of the Configuration class does not return this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. " + this.propertyName
                    + " is expected to return values of type " + this.classTypeName + ".");
        }

        return boolVal;
    }

    /**
     * Set the boolean value associated with the current instance of the class
     * 
     * @param boolVal Boolean value associated with the current instance of the class
     * @throws ConfigurationFormatException
     */
    public void setBoolVal(Boolean boolVal) throws ConfigurationFormatException {
        
        // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(boolean.class.getName())) {

            // The current instance of the Configuration class does not expect this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. Value of type "
                    + this.classTypeName + " was expected for " + this.propertyName + ".");
        }
        
        this.boolVal = boolVal;
    }

    /**
     * Get the class name of the type of value the current instance of the Configuration class handles
     * 
     * @return Class name of the type of value the current instance of the class handles
     */
    public String getClassTypeName() {
        return classTypeName;
    }

    /**
     * Get the date value associated with the current instance of the Configuration class
     * 
     * @return Date value associated with the current instance of the class
     * @throws ConfigurationFormatException 
     */
    public Date getDateVal() throws ConfigurationFormatException {
        
        // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(Date.class.getSimpleName())) {
            
            // The current instance of the Configuration class does not return this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. " + this.propertyName
                    + " is expected to return values of type " + this.classTypeName + ".");
        }
        
        return dateVal;
    }

    /**
     * Get the date value associated with the current instance of the Configuration class
     * 
     * @return Date value associated with the current instance of the class
     * @throws ConfigurationFormatException 
     */
    public String getDateStringVal() throws ConfigurationFormatException {
        
        // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(Date.class.getSimpleName())) {
            
            // The current instance of the Configuration class does not return this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. " + this.propertyName
                    + " is expected to return values of type " + this.classTypeName + ".");
        }

        return this.dateFormat.format(this.dateVal);
    }
    
    /**
     * Set the date value associated with the current instance of the class
     * 
     * @param dateVal Date value associated with the current instance of the class
     * @throws ConfigurationFormatException 
     */
    public void setDateVal(Date dateVal) throws ConfigurationFormatException {
        
        // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(Date.class.getSimpleName())) {

            // The current instance of the Configuration class does not expect this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. Value of type "
                    + this.classTypeName + " was expected for " + this.propertyName + ".");
        }
        
        this.dateVal = dateVal;
    }
    
    /**
     * Set the date value associated with the current instance of the class
     * 
     * @param dateVal Date value associated with the current instance of the class
     * @throws ConfigurationFormatException
     * @throws DateParseException
     */
    public void setDateVal(String dateVal) throws ConfigurationFormatException, DateParseException {

        // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(Date.class.getSimpleName())) {

            // The current instance of the Configuration class does not expect this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. Value of type "
                    + this.classTypeName + " was expected for " + this.propertyName + ".");
        }

        try
        {
            this.dateVal = this.dateFormat.parse(dateVal);
        } catch (ParseException ex) {
            throw new DateParseException(ex);
        }
    }

    /**
     * Get the display flag of the configuration
     * 
     * @return Display flag of the configuration
     */
    public boolean getDisplay() {
        return this.display;
    }
    
    /**
     * Set the display flag of the configuration 
     * 
     * @param display Display flag of the configuration 
     */
    public void setDisplay(boolean display) {
        this.display = display;
    }
            
    /**
     * Get the date value associated with the current instance of the Configuration class
     * 
     * @return Double value associated with the current instance of the class
     * @throws ConfigurationFormatException 
     */
    public Double getDoubleVal() throws ConfigurationFormatException {

         // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(double.class.getName())) {
            
            // The current instance of the Configuration class does not return this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. " + this.propertyName
                    + " is expected to return values of type " + this.classTypeName + ".");
        }
        
        return doubleVal;
    }

    /**
     * Set the double value associated with the current instance of the class
     * 
     * @param doubleVal Double value associated with the current instance of the class
     * @throws ConfigurationFormatException 
     */
    public void setDoubleVal(double doubleVal) throws ConfigurationFormatException {

        // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(double.class.getName())) {

            // The current instance of the Configuration class does not expect this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. Value of type "
                    + this.classTypeName + " was expected for " + this.propertyName + ".");
        }

        this.doubleVal = doubleVal;
    }

    /**
     * Get the float value associated with the current instance of the Configuration class
     * 
     * @return Float value associated with the current instance of the Configuration class
     * @throws ConfigurationFormatException 
     */
    public Float getFloatVal() throws ConfigurationFormatException {

         // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(float.class.getName())) {
            
            // The current instance of the Configuration class does not return this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. " + this.propertyName
                    + " is expected to return values of type " + this.classTypeName + ".");
        }
        
        return floatVal;
    }

    /**
     * Get the float value associated with the current instance of the Configuration class
     * 
     * @return Formatted float value associated with the current instance of the Configuration class
     * @throws ConfigurationFormatException 
     */
    public String getFloatStringVal() throws ConfigurationFormatException {
        
         // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(float.class.getName())) {
            
            // The current instance of the Configuration class does not return this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. " + this.propertyName
                    + " is expected to return values of type " + this.classTypeName + ".");
        }

        return this.decimalFormat.format(floatVal);
    }

    /**
     * Set the float value associated with the current instance of the class
     * 
     * @param floatVal Float value associated with the current instance of the class
     * @throws ConfigurationFormatException 
     */
    public void setFloatVal(float floatVal) throws ConfigurationFormatException {

        // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(float.class.getName())) {

            // The current instance of the Configuration class does not expect this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. Value of type "
                    + this.classTypeName + " was expected for " + this.propertyName + ".");
        }
        
        this.floatVal = floatVal;
    }

    /**
     * Set the float value associated with the current instance of the class
     * 
     * @param floatVal Float value associated with the current instance of the class
     * @throws ConfigurationFormatException
     * @throws ParseException
     */
    public void setFloatVal(String floatVal) throws ConfigurationFormatException, ParseException {

        // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(float.class.getName())) {

            // The current instance of the Configuration class does not expect this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. Value of type "
                    + this.classTypeName + " was expected for " + this.propertyName + ".");
        }
        
        this.floatVal = Float.parseFloat(this.decimalFormat.parse(floatVal).toString());
    }

    /**
     * Get the int value associated with the current instance of the Configuration class
     * 
     * @return Int value associated with the current instance of the Configuration class
     * @throws ConfigurationFormatException 
     */
    public Integer getIntVal() throws ConfigurationFormatException {
        
        // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(int.class.getName())) {
            
            // The current instance of the Configuration class does not return this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. " + this.propertyName
                    + " is expected to return values of type " + this.classTypeName + ".");
        }
        
        return intVal;
    }

    /**
     * Set the int value associated with the current instance of the Configuration class
     * 
     * @param intVal Int value associated with the current instance of the Configuration class
     * @throws ConfigurationFormatException 
     */
    public void setIntVal(int intVal) throws ConfigurationFormatException {
        
        // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(int.class.getName())) {

            // The current instance of the Configuration class does not expect this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. Value of type "
                    + this.classTypeName + " was expected for " + this.propertyName + ".");
        }
        
        this.intVal = intVal;
    }

    /**
     * Whether the current instance of the Configuration class represents an advanced configuration setting
     * 
     * @return Advanced configuration status of the current configuration setting
     */
    public Boolean getIsAdvanced() {
        return isAdvanced;
    }

    /**
     * Get the row position of the configuration in the parameter list window
     * 
     * @return Row position of the configuration
     */
    public int getOrder() {
        return order;
    }
    
    /**
     * Set the row position of the configuration in the parameter list window
     * 
     * @param order Row position of the configuration in the parameter list window
     */
    public void setOrder(int order) {
        this.order = order;
    }

    /**
     * Get the name of the key used in the configuration file
     * 
     * @return Name of the key used in the configuration file
     */
    public String getPropertyName() {
        return propertyName;
    }
    
    /**
     * Get the string value associated with the current instance of the Configuration class
     * 
     * @return String value associated with the current instance of the class
     * @throws ConfigurationFormatException 
     */
    public String getStringVal() throws ConfigurationFormatException {
        
        // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(String.class.getSimpleName())) {
            
            // The current instance of the Configuration class does not return this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. " + this.propertyName
                    + " is expected to return values of type " + this.classTypeName + ".");
        }
        
        return stringVal;
    }

    /**
     * Set the string value associated with the current instance of the class
     * 
     * @param stringVal String value associated with the current instance of the class
     * @throws ConfigurationFormatException 
     */
    public void setStringVal(String stringVal) throws ConfigurationFormatException {

        // Check the data type of the value being stored in the current instance of the Configuration class
        if (!this.classTypeName.equals(String.class.getSimpleName())) {

            // The current instance of the Configuration class does not expect this type of value
            throw new ConfigurationFormatException("Configuration type mismatch error. Value of type "
                    + this.classTypeName + " was expected for " + this.propertyName + ".");
        }

        // Set the string value of the Configuration setting
        this.stringVal = stringVal;
    }

    /**
     * Get the title of a configuration setting
     * 
     * @return Title of a configuration setting
     */
    public String getTitle() {
        return this.title;
    }
    
    /**
     * Configuration class constructor
     * 
     * @param propertyName Name of the key used in the configuration file
     * @param title Title of a configuration setting
     * @param classTypeName Class name of the type of value the current instance of the class handles
     * @param isAdvanced Advanced configuration status of the current configuration setting
     * @param order Row position of the configuration
     * @param display Display flag of the configuration
     */
    public Configuration(String propertyName, String title, String classTypeName, boolean isAdvanced, int order,
            boolean display) {

        // Initialise class attributes
        this.classTypeName = classTypeName;
        this.dateFormat = new SimpleDateFormat(Smurf.DATE_FORMAT);
        this.decimalFormat = (DecimalFormat)NumberFormat.getNumberInstance(Locale.FRENCH);
        this.display = display;
        this.isAdvanced = isAdvanced;
        this.order = order;
        this.propertyName = propertyName;
        this.title = title;

        // Decimal format properties
        this.decimalFormat.applyPattern("##0.00");
        
        // Default values of configuration settings
        this.boolVal = false;
        this.dateVal = null;
        this.doubleVal = Double.MIN_VALUE;
        this.floatVal = Float.MIN_VALUE;
        this.intVal = Integer.MIN_VALUE;
        this.stringVal = "";
    }

    /**
     * Configuration class constructor
     * 
     * @param propertyName Name of the key used in the configuration file
     */
    public Configuration(String propertyName) {

        // Initialise class attributes
        this.propertyName = propertyName;
        this.dateFormat = new SimpleDateFormat(Smurf.DATE_FORMAT);
        this.decimalFormat = (DecimalFormat)NumberFormat.getNumberInstance(Locale.FRENCH);
        
        // Decimal format properties
        this.decimalFormat.applyPattern("##0.00");

        // Default values of configuration settings
        this.boolVal = false;
        this.classTypeName = "String";
        this.dateVal = null;
        this.display = true;
        this.doubleVal = Double.MIN_VALUE;
        this.floatVal = Float.MIN_VALUE;
        this.intVal = Integer.MIN_VALUE;
        this.isAdvanced = false;
        this.order = 0;
        this.stringVal = "";
        this.title = "";
    }

    /**
     * Compare the order of the class to that of the given instance
     * 
     * @param t Given class instance
     * @return Whether the current instance is greater than the given instance
     */
    @Override
    public int compareTo(Configuration t) {

        // Check the order of the current instance to that of the given instance
        if (this.order < t.order) {
            return -1;
        } else if (this.order > t.order) {
            return 1;
        }

        return 0;
    }
    
    /**
     * Compare the current Configuration object instance to that of a given instance using the property name
     * 
     * @param anObject Given Configuration object instance
     * @return Whether the two objects are equal
     */
    @Override
    public boolean equals(Object anObject) {

        // Check if object is being compared to its own instance
        if (this == anObject)
        {
            return true;
        }

        // Check if we are comparing objects of the same type
        if (!(anObject instanceof Configuration)) {
            return false;
        }

        final Configuration configuration = (Configuration)anObject;

        // Compare the property name of both objects
        if ((this.propertyName == null) ? (configuration.propertyName != null) :
                !this.propertyName.equals(configuration.propertyName)) {
            return false;
        }
        
        return true;
    }

    /**
     * Generate a hash code for a Configuration object based on its property name
     * 
     * @return Hash code for a Configuration object
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + (this.propertyName != null ? this.propertyName.hashCode() : 0);
        return hash;
    }
}
