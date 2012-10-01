package smurf.model;

/**
 * The GridConfiguration object represent an application configuration setting that is read from the rubis.properties
 * file and which represents the configuration settings for a column in the list of requests for payment table
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class GridConfiguration implements Comparable<GridConfiguration> {

    private boolean fixed;
    private int columnOrder;
    private int width;
    private String alias;
    private String alignment;
    private String propertyName;
    private String title;

    /**
     * Get the name of the request for payment object property whose value will be displayed in the current column
     * 
     * @return Name of the request for payment object property whose value will be displayed in the current column
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Get the alignment of text within the requests for payment table column
     * 
     * @return Alignment of text within the requests for payment table column
     */
    public String getAlignment() {
        return alignment;
    }

    /**
     * Get the requests for payment table column order
     * @return Requests for payment table column order
     */
    public int getColumnOrder() {
        return columnOrder;
    }

    /**
     * Fixed width column flag of the current column
     * 
     * @return Whether the column can be resized by the user
     */
    public boolean isFixed() {
        return fixed;
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
     * Get the name of the key used in the configuration file
     * 
     * @return Name of the key used in the configuration file
     */
    public String getTitle() {
        return title;
    }

    /**
     * Get the requests for payment table column width
     * 
     * @return Requests for payment table column width
     */
    public int getWidth() {
        return width;
    }

    /**
     * GridConfiguration class constructor
     * 
     * @param propertyName Name of the key used in the configuration file
     * @param title Title of the data grid column
     * @param alias Name of the request for payment object property for which the value will be displayed
     * @param columnOrder Requests for payment table column order
     * @param width Requests for payment table column width
     * @param alignment Alignment of text within the requests for payment table column
     * @param fixed Whether the column can be resized by the user
     */
    public GridConfiguration(String propertyName, String title, String alias, int columnOrder, int width,
            String alignment, boolean fixed) {

        // Initialise class attributes
        this.alias = alias;
        this.alignment = alignment;
        this.columnOrder = columnOrder;
        this.fixed = fixed;
        this.propertyName = propertyName;
        this.title = title;
        this.width = width;
    }

    /**
     * Compare the order of the class to that of the given instance
     * 
     * @param t Given class instance
     * @return Whether the current instance is greater than the given instance
     */
    @Override
    public int compareTo(GridConfiguration t) {

        // Check the order of the current instance to that of the given instance
        if (this.columnOrder < t.columnOrder) {
            return -1;
        } else if (this.columnOrder > t.columnOrder) {
            return 1;
        }

        return 0;
    }

    /**
     * Compare the current GridConfiguration object instance to that of a given instance using the property name
     * 
     * @param obj Given GridConfiguration object instance
     * @return Whether the two objects are equal
     */
    @Override
    public boolean equals(Object obj) {

        // Check if object is being compared to its own instance
        if (obj == null) {
            return false;
        }
        
        // Check if we are comparing objects of the same type
        if (getClass() != obj.getClass()) {
            return false;
        }

        final GridConfiguration other = (GridConfiguration) obj;

        // Compare the property name of both objects
        if ((this.propertyName == null) ? (other.propertyName != null) :
                !this.propertyName.equals(other.propertyName)) {
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
