package smurf.controller;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import smurf.Smurf;
import smurf.dao.ConfigurationDao;
import smurf.exceptions.ConfigurationFormatException;
import smurf.model.Configuration;
import smurf.utilities.Utilities;
import smurf.view.SmurfMainJFrame;

/**
 * The MainWindowController controls the Smurf main application frame and responds to the GUI events.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class MainWindowController extends WindowAdapter implements ComponentListener {

    public final static String CONFIGPANEL = "ConfigPanel";
    public final static String HELPPANEL = "HelpPanel";
    public final static String RUBISPANEL = "RubisPanel";

    private static MainWindowController mainWindowController;
    private ConfigController configController;
    private HelpController helpController;
    private NavigationController navigationController;
    private static SmurfMainJFrame view;
    private RubisController rubisController;
    private StatusController statusController;

    /**
     * Get an instance of the main application window
     * 
     * @return Instance of the main application window
     */
    public static SmurfMainJFrame getView() {
        return view;
    }
    
    /**
     * MainWindowController default constructor
     */
    private MainWindowController() {

        // Initialise class attributes
        view = new SmurfMainJFrame();

        // Set the properties of the main application frame
        view.setLocationRelativeTo(null);
    }

    /**
     * Get an instance of the MainWindowController class
     * 
     * @return MainWindowController class instance
     */
    public static synchronized MainWindowController getMainWindowController() {

        // Check if an instance of the main window controller has already been declared
        if (mainWindowController == null) {
            mainWindowController = new MainWindowController();
        }
        
        return mainWindowController;
    }
    
    /**
     * Display the main application frame
     */
    public void load() {

        // Application view controllers
        configController = new ConfigController();
        helpController = new HelpController();
        navigationController = new NavigationController();
        rubisController = new RubisController();
        statusController = new StatusController();

        // Main application window event listener
        view.addWindowListener(mainWindowController);
        view.addComponentListener(this);

        // Add the navigation panel to the main application window
        view.add(navigationController.getView(), BorderLayout.WEST);

        // Add the status bar panel to the main application window
        view.add(statusController.getView(), BorderLayout.SOUTH);

        // Load the application UI panels
        view.panelsContainer.add(rubisController.getView(), MainWindowController.RUBISPANEL);
        view.panelsContainer.add(configController.getView(), MainWindowController.CONFIGPANEL);
        view.panelsContainer.add(helpController.getView(), MainWindowController.HELPPANEL);

        // Initially display the Rubis panel
        this.displayPanel(MainWindowController.RUBISPANEL);

        // Display the application window
        view.setVisible(true);
    }

    /**
     * Display the panel having the specified panel name
     * 
     * @param panelName Panel name
     */
    public void displayPanel(String panelName) {

        // Show the appropriate panel as per the name panel
        CardLayout cl = (CardLayout) view.panelsContainer.getLayout();
        cl.show(view.panelsContainer, panelName);

        // UI specific actions
        switch (panelName) {

            case MainWindowController.CONFIGPANEL:
                configController.createParamsUi();
                break;

            case MainWindowController.RUBISPANEL:
                rubisController.setupUi();
                break;
        }
    }

    /**
     * Clear generated SMURF output objects for request for payment objects
     */
    public void clearSmurfOutputObjects() {

        // Clear SMURF output objects
        this.rubisController.clearSmurfOuputObjects();
    }

    /**
     * Set the message displayed by the application status bar
     * 
     * @param message Message that needs to be displayed in the application status bar
     */
    public void setStatusBarMessage(String message) {
        statusController.setMessage(message);
    }

    /**
     * Display a modal dialog box with given message and dialog box icon type
     * 
     * @param message Message that the dialog box must be displayed
     * @param messageType Icon that needs to be displayed besides on the application dialog box
     */
    public void showDialogMessage(String message, int messageType) {

        // Show application dialog box
        JOptionPane.showMessageDialog(view, message, "SMURF", messageType);
    }

    /**
     * Handle the application window closing event
     * 
     * @param event Window event
     */
    @Override
    public void windowClosing(WindowEvent event) {

        try {

            // Clear the contents of the temporary folder when closing the application
            ArrayList<Configuration> configurations = ConfigurationDao.getConfigurationDao().getConfigurations();

            // Get temporary folder name index from configurations
            int temporaryFoldernameIndex = configurations.indexOf(new Configuration("folder.temp"));

            // Check if the configuration setting was found
            if (temporaryFoldernameIndex > -1) {

                // Temporary folder path and name
                String tempFoldername = Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator") +
                        configurations.get(temporaryFoldernameIndex).getStringVal();

                // Clear folder content
                Utilities.deleteFolderFiles(tempFoldername);
            }

        } catch (IOException | ConfigurationFormatException ex) {

            // Log error message
            Smurf.logController.log(Level.SEVERE, ConfigurationDao.class.getSimpleName(), ex.getLocalizedMessage());

        } finally {

            // Exit
            System.exit(0);
        }
    }
    
    /**
     * Override the clone method to prevent cloning of the class
     * 
     * @return void
     * @throws CloneNotSupportedException 
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Handle the component resized event of the application window
     * 
     * @param e Event parameters
     */
    @Override
    public void componentResized(ComponentEvent e) {

        // Setup pager for list of requests for payment list based on application window size
        rubisController.setupPager();
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}
