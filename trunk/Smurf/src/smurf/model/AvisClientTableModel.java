package smurf.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.table.AbstractTableModel;
import smurf.Smurf;
import smurf.dao.ConfigurationDao;
import smurf.exceptions.RequestForPaymentAttributeNotFoundException;

/**
 * AvisClientTableModel represents the data model of the data that is shown in the Rubis panel grid 
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class AvisClientTableModel extends AbstractTableModel {

    private String[] columnNames;
    private ArrayList<AvisClient> avisClients;
    private ArrayList<GridConfiguration> gridConfigurations;

    /**
     * AvisClientTableModel constructor
     * 
     * @param avisClients List of requests for payments for which table is being drawn
     */
    public AvisClientTableModel(ArrayList<AvisClient> avisClients) {

        // Define class attributes
        this.avisClients = avisClients;
        this.gridConfigurations = new ArrayList<GridConfiguration>();

        // Get grid configurations from the configuration file
        try {
            this.gridConfigurations = ConfigurationDao.getConfigurationDao().getGridConfigurations();
        } catch (IOException ex) {

            // Write error message to log file
            Smurf.logController.log(Level.SEVERE, ConfigurationDao.class.getSimpleName(), ex.getLocalizedMessage());
        }

        // Define the table column names
        this.columnNames = new String[this.gridConfigurations.size() + 2];

        // Set the default column names
        this.columnNames[0] = "";
        this.columnNames[this.columnNames.length - 1] = "PDF";

        // Set the dynamic column names
        for (int i = 0; i < this.gridConfigurations.size(); i++) {
            this.columnNames[i + 1] = this.gridConfigurations.get(i).getTitle();
        }
    }

    /**
     * Get the number of rows that needs to be displayed
     * 
     * @return Number of rows that needs to be displayed
     */
    @Override
    public int getRowCount() {
        return this.avisClients.size();
    }

    /**
     * Get the number of columns in the grid
     * 
     * @return Number of columns in the grid
     */
    @Override
    public int getColumnCount() {
        return this.columnNames.length;
    }

    /**
     * Get the names of columns in the grid
     * 
     * @param col Column index
     * @return Name of column at the specified index
     */
    @Override
    public String getColumnName(int col) {
        return this.columnNames[col];
    }
    
    /**
     * Get the value of a cell in the grid
     * 
     * @param i Index of the row being rendered
     * @param i1 Index of the column within the specified row being rendered
     * @return Value that needs to be rendered
     */
    @Override
    public Object getValueAt(int i, int i1) {

        Object data = null;
        
        try {

            // Current request for payment
            AvisClient avisClient = this.avisClients.get(i);

            // Get the data that needs to be displayed in the grid
            if (i1 == 0) {

                // Whether the request must be processed
                data = avisClient.getGenerateDocument();

            } else if (i1 == this.columnNames.length - 1) {

                // Generated PDF document file name
                if (avisClient.getPdfFile() != null) {
                    if (avisClient.getPdfFile().length() > 0) {
                        File dummyFile = new File(avisClient.getPdfFile());
                        data = dummyFile.getName();
                    } else {
                        data = "";
                    }
                } else {
                    data = "";
                }

            } else {

                // Get the data that needs to be displayed
                data = avisClient.getFormattedAttribute(this.gridConfigurations.get(i1 - 1).getAlias());

            }

        } catch (RequestForPaymentAttributeNotFoundException ex) {

            // Write error message to log file
            Smurf.logController.log(Level.WARNING, AvisClientTableModel.class.getSimpleName(),
                    ex.getLocalizedMessage());
        }
        
        return data;
    }

    /**
     * Get the class of the data shown by the grid
     * 
     * @param c Index of the data column
     * @return Data class type
     */
    @Override
    public Class getColumnClass(int c) {

        // Default class type
        Class classType = String.class;

        // Set the class type based on the column index
        switch (c) {

            // First column is a boolean
            case 0:
                classType = Boolean.class;
                break;
        }
        
        return classType;
    }

    /**
     * Get the editable status of a cell
     * 
     * @param row Row index
     * @param col Column index
     * @return Whether the cell is editable
     */
    @Override
    public boolean isCellEditable(int row, int col) {

        // The grid first column is editable
        if (col == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the value of the first column in the grid
     * 
     * @param value Value that must be set in the data source
     * @param row Row index
     * @param col Column index
     */
    @Override
    public void setValueAt(Object value, int row, int col) {

        // Value that needs to be set
        boolean booleanValue = Boolean.parseBoolean(value.toString());

        // Get the appropriate request for payment
        AvisClient avisClient = this.avisClients.get(row);

        // Set the generate document status of the request for payment
        avisClient.setGenerateDocument(booleanValue);

        // Fire the table cell update event for the corresponding row and column
        fireTableCellUpdated(row, col);
    }
}
