package smurf;

import java.security.Security;
import java.util.logging.Level;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.log4j.Logger;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import smurf.controller.LogController;
import smurf.controller.MainWindowController;

/**
 * The Smurf class provides the SMURF application entry point
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class Smurf {

    public static LogController logController;
    public static String CONFIG_FILE_NAME = "conf" + System.getProperty("file.separator") + "smurf.properties";
    public static String DATE_FORMAT = "dd/MM/yyyy HH:mm:ss";
    public static String LOG_FILE_NAME = "log" + System.getProperty("file.separator") + "smurf.log";
    public static String REQUEST_FOR_PAYMENT_ID_COL_NAME = "identifiant_avis";
    public static String SMURF_HELP = "smurf_aide.html";

    /**
     * SMURF entry point
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Get instance of the log controller
        logController = LogController.getLogController();

        // Suppress log4j warnings
        Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        // Initialise Apache XML security
        org.apache.xml.security.Init.init();

        // Add Bouncy Castle security provider
        Security.addProvider(new BouncyCastleProvider());

        try {

            // Load SQLite classes
            Class.forName("org.sqlite.JDBC");

            // Set the host OS look and feel for the application
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                UnsupportedLookAndFeelException ex) {
            logController.log(Level.SEVERE, Smurf.class.getSimpleName(), ex.getLocalizedMessage());
        }

        // Main application window controller
        MainWindowController mainWindowController = MainWindowController.getMainWindowController();
        mainWindowController.load();
    }
}