package rubis.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.Timer;
import rubis.view.StatusPanel;

/**
 * StatusController 
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class StatusController implements ActionListener {

    private StatusPanel view;
    private Timer statusBarMessageTimer;

    /**
     * Get the instance of the Status panel view that is controlled by the current instance of the controller
     * 
     * @return Status panel view
     */
    public StatusPanel getView() {
        return this.view;
    }

    /**
     * StatusController default constructor
     */
    public StatusController() {

        // Initialise class attributes
        this.statusBarMessageTimer = new Timer(5000, null);
        this.view = new StatusPanel();
    }

    /**
     * Set the message displayed in the status bar
     * 
     * @param message Message that will be displayed in the status bar
     */
    public void setMessage(String message) {

        // Stop the message reset timer
        this.statusBarMessageTimer.stop();

        // Set the message displayed in the status bar
        this.view.statusLabel.setText(message);

        // Set the action listener of the timer
        this.statusBarMessageTimer.addActionListener(this);

        // Start the status bar message timer
        this.statusBarMessageTimer.start();
    }

    /**
     * Handle the status bar timer firing event
     * 
     * @param e Timer event parameter
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        // Stop the timer
        this.statusBarMessageTimer.stop();
        
        // Reset the message displayed in the status bar
        this.view.statusLabel.setText("SEPAmail RUBIS");

        // Remove any action listener registered to the message timer
        this.statusBarMessageTimer.removeActionListener(this);
    }
}
