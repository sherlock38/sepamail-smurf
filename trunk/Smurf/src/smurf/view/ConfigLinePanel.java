package smurf.view;

/**
 * ConfigLinePanel draws the UI used to allow the user to specify 
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class ConfigLinePanel extends javax.swing.JPanel {

    private Boolean isAdvanced;
    private String propertyKey;
    
    /**
     * Get the advanced status of the panel
     * 
     * @return Whether the panel is being used to display the value of an advanced configuration
     */
    public Boolean getIsAdvanced() {
        return isAdvanced;
    }

    /**
     * Set the advanced status of the panel
     * 
     * @param isAdvanced Whether the panel is being used to display the value of an advanced configuration
     */
    public void setIsAdvanced(Boolean isAdvanced) {
        this.isAdvanced = isAdvanced;
        this.configLabel.setEnabled(!isAdvanced);
        this.configTextField.setEnabled(!isAdvanced);
    }

    /**
     * Get the name of the configuration file property key for which the value is being shown
     * 
     * @return Name of the configuration file property key for which the value is being shown
     */
    public String getPropertyKey() {
        return propertyKey;
    }

    /**
     * Set the name of the configuration file property key for which the value is being shown
     * 
     * @param propertyKey Name of the configuration file property key for which the value is being shown
     */
    public void setPropertyKey(String propertyKey) {
        this.propertyKey = propertyKey;
    }

    /**
     * Creates new form ConfigLinePanel
     */
    public ConfigLinePanel() {
        
        // Initialise class attributes
        this.isAdvanced = false;
        this.propertyKey = "";
        
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        configLabel = new javax.swing.JLabel();
        configTextField = new javax.swing.JTextField();

        setMaximumSize(new java.awt.Dimension(2147483647, 28));
        setMinimumSize(new java.awt.Dimension(300, 28));
        setPreferredSize(new java.awt.Dimension(300, 28));
        setSize(new java.awt.Dimension(300, 28));
        setLayout(new java.awt.BorderLayout());

        configLabel.setText("Paramètre 1");
        configLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 15));
        add(configLabel, java.awt.BorderLayout.LINE_START);

        configTextField.setMaximumSize(new java.awt.Dimension(2147483647, 28));
        add(configTextField, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel configLabel;
    public javax.swing.JTextField configTextField;
    // End of variables declaration//GEN-END:variables
}