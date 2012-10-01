package smurf.controller;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import javax.swing.text.BadLocationException;
import smurf.Smurf;
import smurf.utilities.Utilities;
import smurf.view.HelpPanel;

/**
 * HelpController 
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class HelpController {

    private HelpPanel view;

    /**
     * Get the instance of the Help panel view that is controlled by the current instance of the controller
     * 
     * @return Help panel view
     */
    public HelpPanel getView() {
        return this.view;
    }

    /**
     * HelpController default constructor
     */
    public HelpController() {

        // Initialise class attributes
        this.view = new HelpPanel();

        try {

            // Read help file
            this.loadHelp();

        } catch (BadLocationException ex) {

            // Write error message to log file
            Smurf.logController.log(Level.WARNING, HelpController.class.getSimpleName(), ex.getLocalizedMessage());

        } catch (IOException ex){

            // Write error message to log file
            Smurf.logController.log(Level.WARNING, HelpController.class.getSimpleName(), ex.getLocalizedMessage());
        }
    }

    /**
     * Read the help file and display the content of the file in the help text area
     * 
     * @throws BadLocationException
     * @throws IOException
     */
    private void loadHelp() throws IOException, BadLocationException {

        // Help file
        URL url = new URL("file:///" + Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator")
                + Smurf.SMURF_HELP);

        // Read the help file and load its contents in the help view editor pane
        this.view.helpEditorPane.setPage(url);
    }
}
