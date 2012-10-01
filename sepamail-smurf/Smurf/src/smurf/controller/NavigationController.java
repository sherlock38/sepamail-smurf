package smurf.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JToggleButton;
import smurf.view.NavigationPanel;

/**
 * NavigationController allows for switching between the different panels of the 
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class NavigationController implements ActionListener, ItemListener {

    private MainWindowController mainWindowController;
    private NavigationPanel view;
    private String currentSelectedCommand;

    /**
     * Get the instance of the navigation panel view that is controlled by the current instance of the controller
     * 
     * @return Navigation panel view
     */
    public NavigationPanel getView() {
        return this.view;
    }

    /**
     * NavigationController default constructor
     */
    public NavigationController() {

        // Initialise class attributes
        this.mainWindowController = MainWindowController.getMainWindowController();
        this.view = new NavigationPanel();

        // Action command for the currently selected toggle button
        this.currentSelectedCommand = this.view.rubisButton.getActionCommand();

        // Setup action listeners for buttons
        this.setupButtonActionListeners();
    }

    /**
     * Handle navigation panel toggle button events
     * 
     * @param e Toggle button event object
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        // Check the action command of the button that triggered the action
        if (e.getActionCommand().equals("RUBIS")) {

            // Check if we are not already showing the RUBIS panel
            if (!this.currentSelectedCommand.equals("RUBIS")) {
                
                // Show the RUBIS pane
                this.mainWindowController.displayPanel(MainWindowController.RUBISPANEL);

                // Currently selected toggle button action command
                this.currentSelectedCommand = this.view.rubisButton.getActionCommand();

                // Unselect the help and configurations buttons
                this.view.helpButton.setSelected(false);
                this.view.parameterButton.setSelected(false);
            }

        } else if (e.getActionCommand().equals("CONFIG")) {

            // Check if we are not already showing the configurations panel
            if (!this.currentSelectedCommand.equals("CONFIG")) {

                // Show the configuration panel
                this.mainWindowController.displayPanel(MainWindowController.CONFIGPANEL);

                // Currently selected toggle button action command
                this.currentSelectedCommand = this.view.parameterButton.getActionCommand();

                // Unselect the help and RUBIS buttons
                this.view.helpButton.setSelected(false);
                this.view.rubisButton.setSelected(false);
            }

        } else {

            // Check if we are not already displaying the help panel
            if (!this.currentSelectedCommand.equals("HELP")) {

                // Show the help panel
                this.mainWindowController.displayPanel(MainWindowController.HELPPANEL);

                // Currently selected toggle button action command
                this.currentSelectedCommand = this.view.helpButton.getActionCommand();

                // Unselect the configurations and RUBIS buttons
                this.view.parameterButton.setSelected(false);
                this.view.rubisButton.setSelected(false);
            }
        }
    }

    /**
     * Handle navigation panel toggle button state change events
     * 
     * @param ie Item change event object
     */
    @Override
    public void itemStateChanged(ItemEvent ie) {

        // Toggle button for which the event was triggered
        JToggleButton target = (JToggleButton) ie.getItem();
        
        // Check if we are changing the state of the currently selected toggle button
        if (target.getActionCommand().equals(this.currentSelectedCommand)) {
            target.setSelected(true);
        }
    }
   
    /**
     * Setup action listeners for navigation panel toggle buttons
     */
    private void setupButtonActionListeners() {
        
        // Define action listeners for the navigation panel buttons
        this.view.helpButton.addActionListener(this);
        this.view.helpButton.addItemListener(this);
        this.view.rubisButton.addActionListener(this);
        this.view.rubisButton.addItemListener(this);
        this.view.parameterButton.addActionListener(this);
        this.view.parameterButton.addItemListener(this);
    }
}
