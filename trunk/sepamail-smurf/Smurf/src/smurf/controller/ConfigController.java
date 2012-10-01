package smurf.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.text.ParseException;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import smurf.Smurf;
import smurf.dao.ConfigurationDao;
import smurf.model.Configuration;
import smurf.exceptions.ConfigurationFormatException;
import smurf.exceptions.DateParseException;
import smurf.utilities.Utilities;
import smurf.view.ConfigLinePanel;
import smurf.view.ConfigPanel;

/**
 * ConfigController allows the user to modify and save application configuration settings by using the configuration
 * panel
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class ConfigController implements ActionListener {

    private boolean initialised;
    private ConfigPanel view;
    private ConfigurationDao configurationDao;

    /**
     * Get the instance of the Config panel view that is controlled by the current instance of the controller
     * 
     * @return Config panel view
     */
    public ConfigPanel getView() {
        return this.view;
    }

    /**
     * ConfigController default constructor
     */
    public ConfigController() {
        
        // Configuration UI has not been initialised yet
        this.initialised = false;

        // Configuration settings user interface
        this.view = new ConfigPanel();

        // Initialise class attributes
        try {
            
            // Configuration DAO instance
            this.configurationDao = ConfigurationDao.getConfigurationDao();

        } catch (IOException ex) {

            // Write error message to log file
            Smurf.logController.log(Level.SEVERE, ConfigurationDao.class.getSimpleName(), ex.getLocalizedMessage());

            // Display error message and exit application
            Utilities.errorReadingConfigurationFile();
        }
    }

    /**
     * Create labels and text editors for the application configuration settings
     */
    public void createParamsUi() {

        // Check if the UI has been initialised
        if (!this.initialised && this.configurationDao != null) {

            int maxLabelWidth = 0;
            int labelHeight = 0;

            // Scan the list of configuration objects
            for (int i = 0; i < this.configurationDao.getConfigurations().size(); i++) {

                // Current configuration object
                Configuration configuration = this.configurationDao.getConfigurations().get(i);

                // Check if we need to display the current configuration
                if (configuration.getDisplay()) {

                    // Configuration UI
                    ConfigLinePanel configLinePanel = new ConfigLinePanel();

                    // Set the configuration UI properties
                    configLinePanel.setIsAdvanced(configuration.getIsAdvanced());
                    configLinePanel.setPropertyKey(configuration.getPropertyName());
                    configLinePanel.configLabel.setText(configuration.getTitle());

                    try {

                        // Set the value displayed in the textfield
                        if (configuration.getClassTypeName().equals("boolean")) {
                            configLinePanel.configTextField.setText(configuration.getBoolVal().toString());
                        } else if (configuration.getClassTypeName().equals("Date")) {
                            if (configuration.getDateVal() != null) {
                                configLinePanel.configTextField.setText(configuration.getDateStringVal());
                            }
                        } else if (configuration.getClassTypeName().equals("double")) {
                            configLinePanel.configTextField.setText(configuration.getDoubleVal().toString());
                        } else if (configuration.getClassTypeName().equals("float")) {
                            configLinePanel.configTextField.setText(configuration.getFloatStringVal());
                        } else if (configuration.getClassTypeName().equals("int")) {
                            configLinePanel.configTextField.setText(configuration.getIntVal().toString());
                        } else if (configuration.getClassTypeName().equals("String")) {
                            configLinePanel.configTextField.setText(configuration.getStringVal());
                        } 

                    } catch (ConfigurationFormatException ex) {

                        // Log the error message
                        Smurf.logController.log(Level.WARNING, ConfigController.class.getSimpleName(),
                                ex.getLocalizedMessage());
                    }

                    // Add UI for the current configuration
                    this.view.configsContainerPanel.add(configLinePanel);

                    // Height of a configuration label
                    labelHeight = configLinePanel.configLabel.getPreferredSize().height;

                    // Check the length of the label
                    if (configLinePanel.configLabel.getPreferredSize().width > maxLabelWidth) {

                        // We have a new greater label width
                        maxLabelWidth = configLinePanel.configLabel.getPreferredSize().width;
                    }
                    
                }
            }

            // Set the width of all the labels in the panel for the list of parameters
            for (int i = 0; i < this.view.configsContainerPanel.getComponentCount(); i++) {
                ((ConfigLinePanel)this.view.configsContainerPanel.getComponent(i)).configLabel.setPreferredSize(
                        new Dimension(maxLabelWidth, labelHeight));
            }

            // Button action listeners
            this.view.advancedConfigurationsButton.addActionListener(this);
            this.view.saveButton.addActionListener(this);

            // The panel has been initialised
            this.initialised = true;
        }
    }

    /**
     * Respond button clicks
     * 
     * @param ae Event parameters
     */
    @Override
    public void actionPerformed(ActionEvent ae) {

        // Check the action command of the button that triggered the event
        if (ae.getActionCommand().equals("ADVANCED_CONFIG")) {

            // Enable all the labels and textfields for advanced parameters
            for (int i = 0; i < this.view.configsContainerPanel.getComponentCount(); i++) {
                
                // Current configuration line panel
                ConfigLinePanel configLinePanel = (ConfigLinePanel) this.view.configsContainerPanel.getComponent(i);
 
                // Check if the configuration line panel is displaying values for an advanced parameter
                if (configLinePanel.getIsAdvanced()) {

                    // Enable the configuration line panel controls
                    configLinePanel.configLabel.setEnabled(true);
                    configLinePanel.configTextField.setEnabled(true);
                }
            }
            
            // Change the text and action command of the button
            this.view.advancedConfigurationsButton.setActionCommand("NORMAL_CONFIG");
            this.view.advancedConfigurationsButton.setText("Retour au paramétrage simple");

        } else if (ae.getActionCommand().equals("NORMAL_CONFIG")) {

            // Disable all the labels and textfields for advanced parameters
            for (int i = 0; i < this.view.configsContainerPanel.getComponentCount(); i++) {
                
                // Current configuration line panel
                ConfigLinePanel configLinePanel = (ConfigLinePanel) this.view.configsContainerPanel.getComponent(i);
 
                // Check if the configuration line panel is displaying values for an advanced parameter
                if (configLinePanel.getIsAdvanced()) {

                    // Disable the configuration line panel controls
                    configLinePanel.configLabel.setEnabled(false);
                    configLinePanel.configTextField.setEnabled(false);
                }
            }

            // Change the text and action command of the button
            this.view.advancedConfigurationsButton.setActionCommand("ADVANCED_CONFIG");
            this.view.advancedConfigurationsButton.setText("Paramétrage avancé");

        } else if (ae.getActionCommand().equals("SAVE_CONFIG")) {

            boolean hasErrors = false;
            boolean hasDateErrors = false;

            // Loop through the configuration line panels and set the values of the parameters
            for (int i = 0; i < this.view.configsContainerPanel.getComponentCount(); i++) {

                // Current configuration line panel
                ConfigLinePanel configLinePanel = (ConfigLinePanel) this.view.configsContainerPanel.getComponent(i);

                // Reset the colour of the parameter title
                configLinePanel.configLabel.setForeground(Color.BLACK);

                // Index of the Configuration memory object that corresponds to the value of the configuration line
                int confIndex = this.configurationDao.getConfigurations().indexOf(
                        new Configuration(configLinePanel.getPropertyKey()));

                // Check if the index of the required Configuration object was found
                if (confIndex > -1) {

                    try {

                        // Required configuration object
                        Configuration configuration = this.configurationDao.getConfigurations().get(confIndex);
                        
                        // Set the value of the Conguration object
                        if (configuration.getClassTypeName().equals("boolean")) {

                            // Set the boolean value for the configuration object
                            configuration.setBoolVal(Boolean.parseBoolean(configLinePanel.configTextField.getText()));

                        } else if (configuration.getClassTypeName().equals("Date")) {

                            // Get the trimmed string value specified for the parameter
                            String stringVal = configLinePanel.configTextField.getText().trim();
                            
                            // Set the date value for the configuration object
                            configuration.setDateVal(stringVal);

                        } else if (configuration.getClassTypeName().equals("double")) {

                            // Set the double value for the configuration object
                            configuration.setDoubleVal(Double.parseDouble(configLinePanel.configTextField.getText()));

                        } else if (configuration.getClassTypeName().equals("float")) {

                            // Set the float value for the configuration object
                            configuration.setFloatVal(configLinePanel.configTextField.getText());
                            configLinePanel.configTextField.setText(configuration.getFloatStringVal());

                        } else if (configuration.getClassTypeName().equals("int")) {

                            // Set the integer value for the configuration object
                            configuration.setIntVal(Integer.parseInt(configLinePanel.configTextField.getText()));

                        } else if (configuration.getClassTypeName().equals("String")) {

                            // Get the trimmed string value specified for the parameter
                            String stringVal = configLinePanel.configTextField.getText().trim();

                            // Verify that the string has been specified
                            if (stringVal.length() > 0) {

                                // Set the String value for the configuration object
                                configuration.setStringVal(stringVal);
                                
                                // Check the name of the property that is being saved
                                if (configuration.getPropertyName().equals("mail.type")) {

                                    // Check the value specified
                                    if (!(stringVal.equals("mail") || stringVal.equals("archive"))) {

                                        // The value specified is not valid
                                        throw new ConfigurationFormatException("Value for property "
                                                + configLinePanel.getPropertyKey() + " is not valid. \"mail\" or "
                                                + "\"archive\" is expected.");
                                    }
                                }

                            } else {

                                // We allow blank username and password for database connection settings
                                if (configuration.getPropertyName().equals("database.user") ||
                                        configuration.getPropertyName().equals("database.password")) {

                                    // Set the String value for the configuration object
                                    configuration.setStringVal("");

                                } else {

                                    // The current instance of the Configuration class does not expect the given value
                                    throw new ConfigurationFormatException("Value for property "
                                            + configLinePanel.getPropertyKey() + " of type String has not been "
                                            + "specified.");
                                }
                            }
                        }

                        // Replace the object in the memory array list
                        this.configurationDao.getConfigurations().set(confIndex, configuration);

                    } catch (ConfigurationFormatException ex) {

                        // Parameters panel contains errors
                        hasErrors = true;
 
                        // Write error message to log file
                        Smurf.logController.log(Level.WARNING, ConfigController.class.getSimpleName(),
                                ex.getLocalizedMessage());

                        // Change the colour of the parameter title
                        configLinePanel.configLabel.setForeground(Color.RED);

                    } catch (DateParseException ex) {

                        // Parameters panel contains errors
                        hasErrors = true;
 
                        // Write error message to log file
                        Smurf.logController.log(Level.WARNING, ConfigController.class.getSimpleName(),
                                ex.getLocalizedMessage());

                        // Change the colour of the parameter title
                        configLinePanel.configLabel.setForeground(Color.RED);

                        // We have date format errors
                        hasDateErrors = true;

                    } catch (ParseException ex) {
                        
                        // Parameters panel contains errors
                        hasErrors = true;
 
                        // Write error message to log file
                        Smurf.logController.log(Level.WARNING, ConfigController.class.getSimpleName(),
                                ex.getLocalizedMessage());

                        // Change the colour of the parameter title
                        configLinePanel.configLabel.setForeground(Color.RED);

                    }
                }
            }

            // Verify that the parameters panel does not contain errors
            if (!hasErrors) {

                try {

                    // Save configuration memory objects to file
                    this.configurationDao.saveConfiguration();

                    // Configuration settings have successfully be saved
                    MainWindowController.getMainWindowController()
                            .setStatusBarMessage("Les paramètres ont été sauvegardés avec succès.");

                } catch (IOException ex) {

                    // Save error message to application log
                    Smurf.logController.log(Level.SEVERE, ConfigurationDao.class.getSimpleName(),
                            ex.getLocalizedMessage());

                    // Error message
                    String error = "Une erreur est survenue lors de la sauvegarde des\nparamètres de l'application.";

                    // Display error message to the user
                    MainWindowController.getMainWindowController().showDialogMessage(error, JOptionPane.ERROR_MESSAGE);

                } catch (ConfigurationFormatException ex) {

                    // Save error message to the application log
                    Smurf.logController.log(Level.SEVERE, ConfigurationDao.class.getSimpleName(),
                            ex.getLocalizedMessage());

                    // Error message
                    String error = "Une erreur est survenue lors de la sauvegarde des\nparamètres de l'application.";

                    // Display error message to the user
                    MainWindowController.getMainWindowController().showDialogMessage(error, JOptionPane.ERROR_MESSAGE);
                }
            } else {

                // Error message
                String error = "Les valeurs des paramètres sont erronées. Veuillez\nvérifier les valeurs des"
                        + " paramètres indiquées en rouge\navant de sauvegarder les paramètres de l'application.";

                // Date errors
                if (hasDateErrors) {
                    error += "\n\nLe format de la date accepté est " + Smurf.DATE_FORMAT
                            + ".\nPar exemple, pour spécifer 10 heures le 5 Octobre 2011,\nveuillez renseigner"
                            + "05/10/2011 10:00:00.";
                }

                // Display error message
                MainWindowController.getMainWindowController().showDialogMessage(error, JOptionPane.WARNING_MESSAGE);
            }
        }
    }
}
