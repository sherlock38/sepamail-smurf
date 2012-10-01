package smurf.controller;

import java.awt.Dimension;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import org.apache.commons.lang3.StringUtils;
import smurf.Smurf;
import smurf.dao.AvisClientDao;
import smurf.dao.ConfigurationDao;
import smurf.model.AvisClient;
import smurf.model.AvisClientTableModel;
import smurf.model.GridConfiguration;
import smurf.utilities.SEPAMailDocumentMailer;
import smurf.utilities.SEPAMailDocumentPrinter;
import smurf.view.PDFViewer;
import smurf.view.RubisPanel;
import smurf.view.TaskProgressDialog;

/**
 * RubisController 
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class RubisController extends WindowAdapter implements ActionListener, ItemListener, TableModelListener,
        MouseListener {

    private ArrayList<AvisClient> avisClients;
    private ArrayList<GridConfiguration> gridConfigurations;
    private AvisClientTableModel avisClientTableModel;
    private boolean initialised;
    private HashMap<String, PDFViewer> pdfViewers;
    private int curPageNo;
    private int maxRows;
    private int noDocuments;
    private int noDocumentsSent;
    private int noDocumentsToSend;
    private int noPages;
    private int noSelectedDocuments;
    private RubisPanel view;
    private SwingWorker taskWorker;
    private TaskProgressDialog taskProgressDialog;

    /**
     * Get the instance of the Smurf panel view that is controlled by the current instance of the controller
     * 
     * @return Smurf panel view
     */
    public RubisPanel getView() {
        return this.view;
    }

    /**
     * RubisController default constructor
     */
    public RubisController() {

        this.avisClients = new ArrayList<AvisClient>();
        this.avisClientTableModel = new AvisClientTableModel(this.avisClients);
        this.curPageNo = 0;
        this.gridConfigurations = new ArrayList<GridConfiguration>();
        this.initialised = false;
        this.maxRows = 0;
        this.noDocuments = 0;
        this.noDocumentsSent = 0;
        this.noDocumentsToSend = 0;
        this.noPages = 0;
        this.noSelectedDocuments = 0;
        this.pdfViewers = new HashMap<String, PDFViewer>();
        this.taskProgressDialog = new TaskProgressDialog(MainWindowController.getView(), true);
        this.view = new RubisPanel();

        // Grid column configuration settings
        try {
            this.gridConfigurations = ConfigurationDao.getConfigurationDao().getGridConfigurations();
        } catch (IOException ex) {

            // Write error message to log file
            Smurf.logController.log(Level.SEVERE, ConfigurationDao.class.getSimpleName(), ex.getLocalizedMessage());
        }
    }

    /**
     * Setup the Smurf panel UI
     */
    public void setupUi() {

        // Check if the UI has already been initialised
        if (!this.initialised) {

            // Define the table model of the grid
            this.view.gridTable.setModel(this.avisClientTableModel);

            // Setup the grid which displays the list of requests for payment
            this.setupGrid();

            // Add action event handlers for Rubis panel components
            this.view.firstButton.addActionListener(this);
            this.view.firstStepToggleButton.addActionListener(this);
            this.view.firstStepToggleButton.addItemListener(this);
            this.view.gridTable.addMouseListener(this);
            this.view.lastButton.addActionListener(this);
            this.view.nextButton.addActionListener(this);
            this.view.pagesComboBox.addActionListener(this);
            this.view.previousButton.addActionListener(this);
            this.view.secondStepToggleButton.addActionListener(this);
            this.view.thirdStepToggleButton.addActionListener(this);

            // Set the task progress dialog properties
            this.taskProgressDialog.addWindowListener(this);

            // Add action event handler for the button on the task progress dialog box
            this.taskProgressDialog.taskCancelButton.addActionListener(this);

            // UI has been initialised
            this.initialised = true;
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Event handlers">
    /**
     * Handle action events from the Rubis panel
     * 
     * @param e Action event parameters
     */
    @Override
    public void actionPerformed(ActionEvent e) {

        // Check the action command of the component that triggered the event
        if (e.getActionCommand().equals("RECUPERATION")) {

            // Fetch requests for payment
            this.fetchRequestsForPayment();

        } else if (e.getActionCommand().equals("GENERATION")) {

            // Generate documents for selected requests for payments
            this.generateDocuments();
            
        } else if (e.getActionCommand().equals("CANCEL_FETCHING")) {
            
            // Cancel the task worker thread
            if (this.taskWorker.cancel(true)) {

                // Hide the task progress dialog box
                this.taskProgressDialog.setVisible(false);
            }

        } else if (e.getActionCommand().equals("FIRST_PAGE")) {

            // Show the first page
            this.curPageNo = 0;
            this.showPage();

        } else if (e.getActionCommand().equals("PREVIOUS_PAGE")) {

            // Show the previous page
            this.curPageNo--;

            // Check if we are already at the first page
            if (this.curPageNo < 0) {
                this.curPageNo = 0;
            }

            this.showPage();

        } else if (e.getActionCommand().equals("NEXT_PAGE")) {

            // Show the next page
            this.curPageNo++;

            // Check if we are already at the last page
            if (this.curPageNo > (this.noPages - 1)) {
                this.curPageNo = this.noPages - 1;
            }

            this.showPage();

        } else if (e.getActionCommand().equals("LAST_PAGE")) {

            // Show the last page
            this.curPageNo = this.noPages - 1;
            this.showPage();

        } else if (e.getActionCommand().equals("PAGE_CHANGE")) {

            // Show the appropriate page
            this.curPageNo = this.view.pagesComboBox.getSelectedIndex();
            this.showPage();

        } else if (e.getActionCommand().equals("CANCEL_GENERATION")) {

            // Cancel the task worker thread
            if (this.taskWorker.cancel(true)) {

                // Hide the task progress dialog box
                this.taskProgressDialog.setVisible(false);
            }

        } else if (e.getActionCommand().equals("ENVOI")) {

            // Send generated documents
            this.sendDocuments();

        } else if (e.getActionCommand().equals("CANCEL_SENDING")) {

            // Cancel the task worker thread
            if (this.taskWorker.cancel(true)) {

                // Hide the task progress dialog box
                this.taskProgressDialog.setVisible(false);
            }

        }
    }

    /**
     * Handle Rubis panel toggle buttons state change events
     * 
     * @param ie Item change event object
     */
    @Override
    public void itemStateChanged(ItemEvent ie) {

        // Toggle button for which the event was triggered
        JToggleButton target = (JToggleButton) ie.getItem();

        // Check if we are changing the state of the currently selected toggle button
        if (target.getActionCommand().equals("RECUPERATION")) {

            // Check if requests for payment have been retrieved
            if (this.avisClients.size() > 0) {
                target.setSelected(true);
            } else {
                target.setSelected(false);
            }

        } else if (target.getActionCommand().equals("GENERATION")) {

            // Check if all documents have been generated
            if (this.noDocuments == this.noSelectedDocuments) {
                target.setSelected(true);
            } else {
                target.setSelected(false);
            }
        }
    }

   /**
     * Handle the task progress dialog closing event
     * 
     * @param event Window event
     */
    @Override
    public void windowClosing(WindowEvent event) {

        // Cancel the task worker thread
        if ((!this.taskWorker.isCancelled()) || (!this.taskWorker.isDone())) {
            this.taskWorker.cancel(true);
        }
    }

    /**
     * Setup application pager
     */
    public void setupPager() {

        // Check if the list of requests for payment contains items
        if (this.avisClients.size() > 0) {

            // Setup paging for the list of requests for payment
            calculatePagerValues();

            // Show the first page
            curPageNo = 0;

            // Display the first page
            showPage();
        }
    }

    /**
     * Handle table model change event to update the number of selected documents
     * 
     * @param tme Table model event parameters
     */
    @Override
    public void tableChanged(TableModelEvent tme) {

        // Check if the event is being triggered for generate document status checkbox
        if (tme.getColumn() == 0) {

            // Reset the selected documents counter
            this.noSelectedDocuments = 0;

            // Loop through the list of requests for payment and calculate the list of selected document
            for (int i = 0; i < this.avisClients.size(); i++) {

                // Current request for payment
                AvisClient avisClient = this.avisClients.get(i);

                if (avisClient.getGenerateDocument()) {
                    this.noSelectedDocuments++;
                }
            }
        }
    }

    /**
     * Handle grid mouse click event
     * 
     * @param me Mouse event parameters
     */
    @Override
    public void mouseClicked(MouseEvent me) {

        // Check for double click
        if (me.getClickCount() == 2) {

            // Target grid
            JTable target = (JTable)me.getSource();

            // Selected row and column
            int column = target.getSelectedColumn();
            int row = target.getSelectedRow();

            // Check if the PDF filename column has been double clicked
            if (column == this.avisClientTableModel.getColumnCount() - 1) {

                AvisClient avisClient = this.avisClients.get(row + (this.maxRows * this.curPageNo));

                // Check if filename has been defined
                if (avisClient.getPdfFile() != null) {

                    // PDF file object
                    File pdfFile = new File(avisClient.getPdfFile());

                    // Check if the PDF file exists
                    if (pdfFile.exists() && pdfFile.isFile()) {

                        // Check if we have a PDF viewer for the current PDF file
                        if (this.pdfViewers.containsKey(pdfFile.getName())) {

                            // Get the instance of the required viewer
                            PDFViewer pdfViewer = this.pdfViewers.get(pdfFile.getName());

                            // Bring the already opened PDF viewer to front
                            pdfViewer.setVisible(true);
                            pdfViewer.toFront();

                        } else {

                            // Create a PDF viewer for the PDF file
                            PDFViewer pdfViewer = new PDFViewer();

                            // Set PDF viewer properties
                            pdfViewer.setTitle(pdfFile.getName());

                            // Open the request for payment PDF document
                            pdfViewer.pdfViewerController.openDocument(pdfFile.getAbsolutePath());

                            // Add the PDF viewer to the map of viewers
                            this.pdfViewers.put(pdfFile.getName(), pdfViewer);

                            // Show the PDF viewer
                            pdfViewer.setVisible(true);
                            pdfViewer.toFront();
                        }

                    } else {

                        MainWindowController.getMainWindowController().showDialogMessage(
                                "Une erreur est survenue lors de la lecture du\ndocument de demande de règlement.",
                                JOptionPane.ERROR_MESSAGE);

                        // Write error message to log file
                        Smurf.logController.log(Level.WARNING, PDFViewer.class.getSimpleName(),
                                "The file " + pdfFile.getAbsolutePath() + " could not be read.");
                    }
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent me) {

    }

    @Override
    public void mouseReleased(MouseEvent me) {

    }

    @Override
    public void mouseEntered(MouseEvent me) {

    }

    @Override
    public void mouseExited(MouseEvent me) {

    }
    
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Grid manipulations">
    /**
     * Reset the grid of requests for payment
     */
    private void resetGrid() {

        // Reset the list of request for payment
        this.avisClients.clear();
        this.avisClientTableModel = new AvisClientTableModel(this.avisClients);
        this.view.gridTable.setModel(this.avisClientTableModel);

        // Reset the paginator
        this.view.firstButton.setEnabled(false);
        this.view.previousButton.setEnabled(false);
        this.view.nextButton.setEnabled(false);
        this.view.lastButton.setEnabled(false);
        this.view.pagesComboBox.removeAllItems();
        this.view.pagesComboBox.setEnabled(false);
        this.view.pagerLabel.setText("");

        // Reset counters
        this.curPageNo = 0;
        this.noDocuments = 0;
        this.noDocumentsSent = 0;
        this.noDocumentsToSend = 0;
        this.noPages = 0;
        this.noSelectedDocuments = 0;

        // Reset allowed operations
        this.view.secondStepToggleButton.setEnabled(false);
        this.view.thirdStepToggleButton.setEnabled(false);
    }
    
    /**
     * Setup the grid used to display the list of requests for payment
     */
    private void setupGrid() {

        // Table header
        JTableHeader tableHeader = this.view.gridTable.getTableHeader();

        // Set table header properties
        tableHeader.setPreferredSize(new Dimension(tableHeader.getPreferredSize().width, 22));

        
        // Set the table column styles
        TableColumn tableColumn;
        for (int i = 0; i < this.avisClientTableModel.getColumnCount(); i++) {

            // Current table column
            tableColumn = this.view.gridTable.getColumnModel().getColumn(i);

            // Column width
            if (i == 0) {
                
                // Check box column width
                tableColumn.setMaxWidth(30);
                tableColumn.setMinWidth(30);
                tableColumn.setPreferredWidth(30);

            } else if (i == this.avisClientTableModel.getColumnCount() - 1) {

                // Properties of the column displaying generated PDF document file name
                tableColumn.setPreferredWidth(200);

            } else {

                // Dynamic column settings
                GridConfiguration gridConfiguration = this.gridConfigurations.get(i - 1);

                // Set the width of the column
                tableColumn.setPreferredWidth(gridConfiguration.getWidth());

                // Check if the column is fixed
                if (gridConfiguration.isFixed()) {
                    tableColumn.setMaxWidth(gridConfiguration.getWidth());
                    tableColumn.setMinWidth(gridConfiguration.getWidth());
                }

                // Set the column alignment
                if (gridConfiguration.getAlignment().toLowerCase().equals("center")) {

                    // Cell renderer for table column cell
                    DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
                    centerRenderer.setHorizontalAlignment(JLabel.CENTER);
                    tableColumn.setCellRenderer(centerRenderer);

                } else if (gridConfiguration.getAlignment().toLowerCase().equals("right")) {

                    // Cell renderer for table column cell
                    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
                    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
                    tableColumn.setCellRenderer(rightRenderer);
                }
            }
        }

        // Set table properties
        this.view.gridTable.setRowHeight(20);
        this.view.gridTable.setShowGrid(true);
        this.view.gridTable.setShowHorizontalLines(false);
        this.view.gridTable.setShowVerticalLines(true);
    }
    
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="Fetch requests for payment with error handling">
    /**
     * Fetch requests for payment from the predefined database
     */
    private void fetchRequestsForPayment() {

        // Reset the grid of requests for payment and the paginator
        this.resetGrid();

        // Create a Swing worker thread for fetching the list of payment requests
        this.taskWorker = new SwingWorker<ArrayList<AvisClient>, String>() {

            /**
              * Fetch the list of requests for payment
              * 
              * @return List of requests for payment
              * @throws Exception
              */
            @Override
            protected ArrayList<AvisClient> doInBackground() throws Exception {

                // Initialise class attributes
                AvisClientDao avisClientDao = new AvisClientDao();
            
                return avisClientDao.getAvisClients();
            }

            /**
             * Retrieve the list of requests for payment and hide the task progress dialog box when the background task
             * is completed
             */
            @Override
            public void done() {

                try {

                    // Hide the task progress indicator dialog box
                    taskProgressDialog.setVisible(false);

                    // Get the list of requests for payment
                    avisClients = get();

                    // Display the list of requests for payment
                    if (avisClients.size() > 0) {

                        // Number of selected documents
                        noSelectedDocuments = avisClients.size();

                        // Indicate that requests for payment has been retrieved
                        view.firstStepToggleButton.setSelected(true);

                        // Display number of requests fetched
                        if (avisClients.size() == 1) {
                            MainWindowController.getMainWindowController().setStatusBarMessage("Une demande a été "
                                    + "récupérée de la base de données.");
                        } else {
                            MainWindowController.getMainWindowController().setStatusBarMessage(avisClients.size()
                                    + " demandes ont été récupérées de la base de données.");
                        }

                        // Setup paging for the list of requests for payment
                        calculatePagerValues();

                        // Show the first page
                        curPageNo = 0;

                        // Display the first page
                        showPage();

                        // Allow user to generate documents for requests of payment
                        view.secondStepToggleButton.setEnabled(true);

                    } else {

                        MainWindowController.getMainWindowController().setStatusBarMessage("Aucune demande de "
                                + "règlement n’a été récupérée de la base de données.");

                        // Indicate that the list of requests for payment is empty
                        view.firstStepToggleButton.setSelected(false);
                    }

                } catch (CancellationException ex) {
                    
                    // Write error message to log file
                    Smurf.logController.log(Level.INFO, SwingWorker.class.getSimpleName(), "The fetching of requests "
                            + "for payment from the database was cancelled by the user.");

                } catch (InterruptedException ex) {

                    // Write error message to log file
                    Smurf.logController.log(Level.SEVERE, SwingWorker.class.getSimpleName(), ex.getLocalizedMessage());

                } catch (ExecutionException ex) {

                    // Check if an SQL exception was raised
                    if (StringUtils.countMatches(ex.getMessage(), "jdbc") > 0) {

                        MainWindowController.getMainWindowController().showDialogMessage(
                                "Une erreur est survenue lors de la récupération des\ndemandes de"
                                + " règlement.\n\nVeuillez vérifier les paramètres de connexion à la\nbase"
                                + " de données avant de relancer la récupération.",
                                JOptionPane.ERROR_MESSAGE);

                        // Write error message to log file
                        Smurf.logController.log(Level.SEVERE, AvisClientDao.class.getSimpleName(),
                                ex.getLocalizedMessage());

                    } else if (ex.getMessage().startsWith("java.sql")) {

                        MainWindowController.getMainWindowController().showDialogMessage(
                                "Une erreur est survenue lors de la récupération des\ndemandes de"
                                + " règlement.\n\nVeuillez vérifier les paramètres de connexion à la\nbase"
                                + " de données avant de relancer la récupération.",
                                JOptionPane.ERROR_MESSAGE);

                        // Write error message to log file
                        Smurf.logController.log(Level.SEVERE, AvisClientDao.class.getSimpleName(),
                                ex.getLocalizedMessage());

                    } else if (ex.getMessage().startsWith("java.io.IOException")) {

                        // Display error message
                        MainWindowController.getMainWindowController().showDialogMessage(
                                "Une erreur est survenue lors de la lecture du fichier\nde configuration de "
                                + "l'application. Veuillez vérifier\nque le fichier est à l'emplacement requi avant "
                                + "de\nrelancer l'application.",
                                JOptionPane.WARNING_MESSAGE);

                        // Write error message to log file
                        Smurf.logController.log(Level.SEVERE, ConfigurationDao.class.getSimpleName(),
                                ex.getLocalizedMessage());

                    } else if (ex.getMessage().startsWith("smurf.exceptions.ConfigurationFormatException")) {

                        MainWindowController.getMainWindowController().showDialogMessage(
                                "Les paramètres de connexion à la base de données semblent être erronés.\n\n"
                                + "Veuillez vérifier ces paramètres avec de relancer la récupération.",
                                JOptionPane.ERROR_MESSAGE);

                        // Write error message to log file
                        Smurf.logController.log(Level.SEVERE, AvisClientDao.class.getSimpleName(),
                                ex.getLocalizedMessage());
            
                    } else if (ex.getMessage().startsWith("smurf.exceptions.StartDateNotSpecifiedException")) {

                        MainWindowController.getMainWindowController().showDialogMessage(
                                "La date de début des demandes de règlement n'a\npas été spécifiée.\n\n"
                                + "Veuillez renseigner la date de début des demandes\nde règlement avant de relancer "
                                + "la récupération.",
                                JOptionPane.ERROR_MESSAGE);

                        // Write error message to log file
                        Smurf.logController.log(Level.WARNING, AvisClientDao.class.getSimpleName(),
                                ex.getLocalizedMessage());

                    } else if (ex.getMessage().startsWith("smurf.exceptions.EndDateNotSpecifiedException")) {

                        MainWindowController.getMainWindowController().showDialogMessage(
                                "La date de fin des demandes de règlement n'a\npas été spécifiée.\n\n"
                                + "Veuillez renseigner la date de fin des demandes\nde règlement avant de relancer "
                                + "la récupération.",
                                JOptionPane.ERROR_MESSAGE);

                        // Write error message to log file
                        Smurf.logController.log(Level.WARNING, AvisClientDao.class.getSimpleName(),
                                ex.getLocalizedMessage());

                    } else if (ex.getMessage().startsWith("smurf.exceptions.DatesNotSpecifiedException")) {

                        MainWindowController.getMainWindowController().showDialogMessage(
                                "Les date de début et de fin des demandes de règlement n'ont\npas été "
                                + "spécifiées.\n\nVeuillez renseigner les dates de début et de fin des demandes\n"
                                + "de règlement avant de relancer la récupération.",
                                JOptionPane.ERROR_MESSAGE);

                        // Write error message to log file
                        Smurf.logController.log(Level.WARNING, AvisClientDao.class.getSimpleName(),
                                ex.getLocalizedMessage());

                    } else {

                        // Write error message to log file
                        Smurf.logController.log(Level.WARNING, AvisClientDao.class.getSimpleName(),
                                ex.getLocalizedMessage());

                    }
                }
            }
        };

        // Show the dialog indicating that payment requests are being retrieved
        this.taskProgressDialog.taskCancelButton.setActionCommand("CANCEL_FETCHING");
        this.taskProgressDialog.taskCancelButton.setText("Arrêter la récupération");
        this.taskProgressDialog.taskDescriptionLabel.setText("La récupération des demandes de règlement est en cours");
        this.taskProgressDialog.taskProgressBar.setIndeterminate(true);
        this.taskProgressDialog.setLocationRelativeTo(MainWindowController.getView());

        // Start the worker thread which fetches request for payments and show the task progress indicator
        this.taskWorker.execute();
        this.taskProgressDialog.setVisible(true);

    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Paginator methods">
    /**
     * Calculate pager values
     */
    private void calculatePagerValues() {

        // Maximum number of rows that can be displayed in the grid
        this.maxRows = (int)Math.floor((this.view.gridScrollPane.getSize().height - 22) / 20.0);

        // Number of pages required to display all the requests for payment
        this.noPages = (this.avisClients.size() - (this.avisClients.size() % this.maxRows)) / this.maxRows;

        // Adjust the number of pages
        if (this.avisClients.size() % this.maxRows != 0) {
            this.noPages++;
        }

        // Clear the page list
        this.view.pagesComboBox.removeAllItems();

        // Add pages to the page list
        for (int i = 0; i < this.noPages; i++) {
            this.view.pagesComboBox.addItem("Page " + (i + 1));
        }

        // Select the first item
        if (this.view.pagesComboBox.getItemCount() > 0) {
            this.view.pagesComboBox.setSelectedIndex(0);
        }

        // Enable the page selector if we have more than one page
        if (this.noPages > 1) {
            this.view.pagesComboBox.setEnabled(true);
        }
    }

    /**
     * Get a subset of the current list of requests for payment
     * 
     * @param startupIndex Index of the first element of the subset in the current list
     * @param length Number of elements in the subset
     * @return A subset of the current list of requests for payment
     */
    private ArrayList<AvisClient>getListSubset(int startupIndex, int length) {

        ArrayList<AvisClient> subAvisClients = new ArrayList<AvisClient>();

        // Check if the start up index for the subset is valid
        if ((startupIndex > -1) && (startupIndex < this.avisClients.size())) {

            // Check the number of elements available
            if ((this.avisClients.size() - startupIndex) > (length - 1)) {

                // Build the subset with the expected number of elements
                for (int i = 0; i < length; i++) {
                    subAvisClients.add(this.avisClients.get(i + startupIndex));
                }

            } else {

                // Build the subset with the remaining number of elements
                for (int i = 0; i < (this.avisClients.size() - startupIndex); i++) {
                    subAvisClients.add(this.avisClients.get(i + startupIndex));
                }
            }
        }

        return subAvisClients;
    }

    /**
     * Display a page for the list of requests for payments
     */
    private void showPage() {

        // Setup the table model for the current page
        this.avisClientTableModel = new AvisClientTableModel(this.getListSubset(this.curPageNo * this.maxRows,
                this.maxRows));

        // Display the rows for the current page
        this.view.gridTable.setModel(this.avisClientTableModel);

        // Listener for changes made to the model
        this.view.gridTable.getModel().addTableModelListener(this);

        // Setup the grid
        this.setupGrid();

        // Update pager components
        if (this.noPages > 1) {

            if (this.curPageNo == 0) {
                
                // We are displaying the first page so allow the user to move to the next pages
                this.view.firstButton.setEnabled(false);
                this.view.previousButton.setEnabled(false);
                this.view.nextButton.setEnabled(true);
                this.view.lastButton.setEnabled(true);

            } else if (this.curPageNo == (this.noPages - 1)) {

                // We are displaying the last page so allow the user to move to the previous pages
                this.view.firstButton.setEnabled(true);
                this.view.previousButton.setEnabled(true);
                this.view.nextButton.setEnabled(false);
                this.view.lastButton.setEnabled(false);
            } else {

                // We are displaying middle pages, so allow user to move freely
                this.view.firstButton.setEnabled(true);
                this.view.previousButton.setEnabled(true);
                this.view.nextButton.setEnabled(true);
                this.view.lastButton.setEnabled(true);
            }
        }

        // Update pages list component
        this.view.pagesComboBox.setSelectedIndex(this.curPageNo);

        // Pager message
        String message;
        if (this.view.gridTable.getRowCount() == 1) {
            message = "Demande " + ((this.curPageNo * this.maxRows) + 1);
        } else {
            message = "Demandes " + ((this.curPageNo * this.maxRows) + 1) + " à "
                    + ((this.curPageNo * this.maxRows) + this.view.gridTable.getRowCount());
        }
        message += " sur " + this.avisClients.size();
        this.view.pagerLabel.setText(message);

    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Generate documents for selected requests for payment">
    /**
     * Generate documents
     */
    private void generateDocuments() {

        // Check if documents have been selected
        if (this.noSelectedDocuments > 0) {

            // Define background worker to generate required documents for selected requests for payment
            this.taskWorker = new SwingWorker<Integer, Integer>() {

                /**
                 * Generate required documents for the list of selected requests for payment
                 * 
                 * @return void
                 * @throws Exception
                 */
                @Override
                protected Integer doInBackground() throws Exception {

                    int generatedDocumentCount = 0;

                    // Create instance of document printer class
                    SEPAMailDocumentPrinter printer = new SEPAMailDocumentPrinter();

                    // Scan the list of requests for payment
                    for (int i = 0; i < avisClients.size(); i++) {

                        // Check if the user has cancelled the thread
                        if (!isCancelled()) {

                            // Current request for payment
                            AvisClient avisClient = avisClients.get(i);

                            // Check if a document must be generated for the current request for payment
                            if (avisClient.getGenerateDocument()) {

                                // Generate the required PDF file and set related PDF file property
                                avisClient.setPdfFile(printer.generateRequestForPaymentDocument(avisClient));

                                // Increment the count for number of documents generated
                                generatedDocumentCount++;

                                // Update the UI to reflect the percentage work completed
                                publish(generatedDocumentCount);
                            }

                        } else {

                            // Exit loop if process is cancelled
                            break;
                        }
                    }
 
                    return generatedDocumentCount;
                }

                /**
                 * Update the UI to reflect the percentage work completed
                 * 
                 * @param noGeneratedDocuments Number of documents already generated
                 */
                @Override
                protected void process(List<Integer> noGeneratedDocuments) {

                    // Update the task progress indicator
                    taskProgressDialog.taskProgressBar.setValue(noGeneratedDocuments.get(0));

                    // Update the grid of requests for payment
                    if (avisClientTableModel != null) {
                        avisClientTableModel.fireTableDataChanged();
                    }
                }

                /**
                 * Handle exceptions that might have been raised while generating documents
                 */
                @Override
                public void done() {

                    try {

                        // Check if task was not cancelled
                        if (!isCancelled()) {

                            // Indicate that task was fully completed
                            taskProgressDialog.taskProgressBar.setValue(noSelectedDocuments);
                        }

                        // Hide the task progress indicator dialog box
                        taskProgressDialog.setVisible(false);

                        // Retrieve the total number of documents generated
                        noDocuments = get();

                        // Indicate that all documents have been generated
                        if (noDocuments == noSelectedDocuments) {
                            view.secondStepToggleButton.setSelected(true);
                        } else {
                            view.secondStepToggleButton.setSelected(false);
                        }

                        // Check if documents can be sent to mail server
                        if (noDocuments > 0) {

                            // Allow user to generate documents for requests of payment
                            view.thirdStepToggleButton.setEnabled(true);

                            // Display number of documents generated
                            if (noDocuments == 1) {
                                MainWindowController.getMainWindowController().setStatusBarMessage("Un document de"
                                        + " demande de règlement a été généré.");
                            } else {
                                MainWindowController.getMainWindowController().setStatusBarMessage(noDocuments
                                        + " documents de demandes de règlement ont été générés.");
                            }
                        }

                    } catch (CancellationException ex) {

                        // Write error message to log file
                        Smurf.logController.log(Level.INFO, SwingWorker.class.getSimpleName(), "The generation of"
                                + " documents for selected requests for payment was cancelled by the user.");

                    } catch (InterruptedException ex) {

                        // Write error message to log file
                        Smurf.logController.log(Level.SEVERE, SwingWorker.class.getSimpleName(),
                                ex.getLocalizedMessage());

                    } catch (ExecutionException ex) {
                        
                        // Invalid template path exception
                        if (ex.getMessage().startsWith("smurf.exceptions.InvalidTemplatePathException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Le dossier des gabarits spécifié ne semble pas être valide.\nVeillez vérifer "
                                    + "le nom du dossier des gabarits spécifié\navant de redémarrer l'opération.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, RubisController.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else if (ex.getMessage().startsWith("smurf.exceptions.RequestForPaymentTemplateNotFoundException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Le gabarit pour le document des demandes de règlement est introuvable.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, RubisController.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else if (ex.getMessage().startsWith("smurf.exceptions.RequestForPaymentTemplateNotDefinedException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Le gabarit pour le document des demandes de règlement n'a pas été défini.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, RubisController.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else if (ex.getMessage().startsWith("smurf.exceptions.SepaMailTemplateNotDefinedException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Le gabarit pour le fichier missive SEPAmail n'a pas été défini.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, RubisController.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else if (ex.getMessage().startsWith("smurf.exceptions.SepaMailTemplateNotFoundException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Le gabarit pour le fichier missive SEPAmail est introuvable.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, RubisController.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else if (ex.getMessage().startsWith("java.io.IOException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Une erreur est survenue lors de la génération du document de demande de règlement.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else if (ex.getMessage().startsWith("java.io.FileNotFoundException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Une erreur est survenue lors de la génération du document de demande de règlement.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else if (ex.getMessage().startsWith("com.itextpdf.text.DocumentException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Une erreur est survenue lors de la génération du document de demande de règlement.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else if (ex.getMessage().startsWith("java.awt.print.PrinterException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Une erreur est survenue lors de la génération du document de demande de règlement.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else {

                            // Log any exceptions that have not been catered for
                            Smurf.logController.log(Level.WARNING, RubisController.class.getSimpleName(),
                                    ex.getLocalizedMessage());
                        }

                    } finally {

                        // Update the grid of requests for payment
                        if (avisClientTableModel != null) {
                            avisClientTableModel.fireTableDataChanged();
                        }
                    }
                }
            };

            // Show the dialog indicating that documents are being generated
            this.taskProgressDialog.taskCancelButton.setActionCommand("CANCEL_GENERATION");
            this.taskProgressDialog.taskCancelButton.setText("Arrêter la génération");
            this.taskProgressDialog.taskDescriptionLabel.setText("La génération des demandes de règlement est en"
                    + "cours");
            this.taskProgressDialog.taskProgressBar.setIndeterminate(false);
            this.taskProgressDialog.taskProgressBar.setMinimum(0);
            this.taskProgressDialog.taskProgressBar.setMaximum(this.noSelectedDocuments);
            this.taskProgressDialog.taskProgressBar.setValue(0);
            this.taskProgressDialog.setLocationRelativeTo(MainWindowController.getView());

            // Start the worker thread which generates documents for requests for payment and show the progress
            // indicator
            this.taskWorker.execute();
            this.taskProgressDialog.setVisible(true);

        } else {

            // Remind user to select at least one document
            MainWindowController.getMainWindowController().showDialogMessage("Veuillez sélecter au moins une demande de"
                    + " règlement\npour laquelle le document requi sera généré.", JOptionPane.WARNING_MESSAGE);
        }

    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Send generated documents">
    
    /**
     * Send documents
     */
    private void sendDocuments() {

        this.noDocumentsToSend = 0;

        // Check the number of requests for which documents can be sent
        for (int i = 0; i < this.avisClients.size(); i++) {
            
            // Current request for payment
            AvisClient avisClient = this.avisClients.get(i);

            if (avisClient.getGenerateDocument()) {
                if (avisClient.getPdfFile() != null) {
                    this.noDocumentsToSend++;
                }
            }
        }

        // Check if we have documents that can be sent
        if (this.noDocumentsToSend > 0) {

            // Define background worker to send requests for payment documents
            this.taskWorker = new SwingWorker<Integer, Integer>() {

                /**
                 * Send request for payment documents
                 * 
                 * @return void
                 * @throws Exception
                 */
                @Override
                protected Integer doInBackground() throws Exception {

                    int noSentDocuments = 0;

                    // Create instance of document mailer
                    SEPAMailDocumentMailer mailer = new SEPAMailDocumentMailer();

                    // Loop the requests for payment documents and email those documents
                    for (int i = 0; i < avisClients.size(); i++) {

                        // Check if the user has cancelled the thread
                        if (!isCancelled()) {

                            // Current request for payment
                            AvisClient avisClient = avisClients.get(i);

                            // Check if we need to send the document for the current request for payment
                            if (avisClient.getGenerateDocument()) {

                                // Check if the PDF file can be read
                                if (avisClient.getPdfFile() != null) {

                                    // Send document for the current request for payment
                                    mailer.sendDocumentForRequest(avisClient);

                                    // Increase the number of documents processed
                                    noSentDocuments++;

                                    // Update the UI to reflect the percentage work completed
                                    publish(noSentDocuments);
                                }
                            }

                        } else {
                            break;
                        }
                    }

                    // Generate the sent documents log
                    mailer.createMailLog();
                    
                    return noSentDocuments;
                }

                /**
                 * Update the UI to reflect the percentage work completed
                 * 
                 * @param noSentDocuments Number of documents already sent
                 */
                @Override
                protected void process(List<Integer> noSentDocuments) {

                    // Update the task progress indicator
                    taskProgressDialog.taskProgressBar.setValue(noSentDocuments.get(0));

                }

                /**
                 * Handle exceptions that might have been raised while sending documents
                 */
                @Override
                public void done() {

                    try {

                        // Check if task was not cancelled
                        if (!isCancelled()) {

                            // Indicate that task was fully completed
                            taskProgressDialog.taskProgressBar.setValue(noDocumentsToSend);
                        }

                        // Hide the task progress indicator dialog box
                        taskProgressDialog.setVisible(false);

                        // Retrieve the total number of documents sent
                        noDocumentsSent = get();

                        // Indicate that all documents have been generated
                        if (noDocumentsSent == noDocumentsToSend) {
                            view.thirdStepToggleButton.setSelected(true);
                        } else {
                            view.thirdStepToggleButton.setSelected(false);
                        }

                        if (noDocumentsSent > 0) {

                            // Display number of sent documents
                            if (noDocumentsSent == 1) {
                                 MainWindowController.getMainWindowController().setStatusBarMessage("Un document de"
                                        + "demande de règlement a été envoyé.");
                            } else {
                                 MainWindowController.getMainWindowController().setStatusBarMessage(noDocumentsSent
                                        + " documents de demande de règlement ont été envoyés.");
                            }
                        }

                    } catch (CancellationException ex) {

                        // Write error message to log file
                        Smurf.logController.log(Level.INFO, SwingWorker.class.getSimpleName(), "The sending of"
                                + " selected requests for payment documents was cancelled by the user.");

                    } catch (InterruptedException ex) {

                        // Write error message to log file
                        Smurf.logController.log(Level.SEVERE, SwingWorker.class.getSimpleName(),
                                ex.getLocalizedMessage());

                    } catch (ExecutionException ex) {

                        if (ex.getMessage().startsWith("smurf.exceptions.LogTemplateNotDefinedException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Le gabarit du fichier de journalisation des envois de document n'a pas été défini.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, RubisController.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else if (ex.getMessage().startsWith("smurf.exceptions.LogTemplateNotFoundException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Le gabarit du fichier de journalisation est introuvable.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, RubisController.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else if (ex.getMessage().startsWith("smurf.exceptions.MailParameterNotDefinedException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Un ou plusieurs paramètres requis pour l'envoi des documents n'ont pas été renseignés.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, RubisController.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else if (ex.getMessage().startsWith("javax.mail.MessagingException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Une erreur est survenue lors de l'envoi des documents de demandes de règlement.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else if (ex.getMessage().startsWith("java.io.IOException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Une erreur est survenue lors de la génération du fichier de journalisation\n"
                                    + "de l'envoi des documents de demandes de règlement.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else if (ex.getMessage().startsWith("java.io.FileNotFoundException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Une erreur est survenue lors de la génération du fichier de journalisation\n"
                                    + "de l'envoi des documents de demandes de règlement.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else if (ex.getMessage().startsWith("com.itextpdf.text.DocumentException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Une erreur est survenue lors de la génération du fichier de journalisation\n"
                                    + "de l'envoi des documents de demandes de règlement.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else if (ex.getMessage().startsWith("java.awt.print.PrinterException")) {

                            MainWindowController.getMainWindowController().showDialogMessage(
                                    "Une erreur est survenue lors de la génération du fichier de journalisation\n"
                                    + "de l'envoi des documents de demandes de règlement.",
                                    JOptionPane.ERROR_MESSAGE);

                            // Write error message to log file
                            Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                                    ex.getLocalizedMessage());

                        } else {

                            // Log any exceptions that have not been catered for
                            Smurf.logController.log(Level.WARNING, RubisController.class.getSimpleName(),
                                    ex.getLocalizedMessage());
                        }
                    }
                }
            };

            // Show the dialog indicating that documents are being sent
            this.taskProgressDialog.taskCancelButton.setActionCommand("CANCEL_SENDING");
            this.taskProgressDialog.taskCancelButton.setText("Arrêter l'envoi");
            this.taskProgressDialog.taskDescriptionLabel.setText("L'envoi des documents est en cours");
            this.taskProgressDialog.taskProgressBar.setIndeterminate(false);
            this.taskProgressDialog.taskProgressBar.setMinimum(0);
            this.taskProgressDialog.taskProgressBar.setMaximum(this.noDocumentsToSend);
            this.taskProgressDialog.taskProgressBar.setValue(0);
            this.taskProgressDialog.setLocationRelativeTo(MainWindowController.getView());

            // Start the worker thread which sends requests for payment documents and show the progress indicator
            this.taskWorker.execute();
            this.taskProgressDialog.setVisible(true);

        } else {

            // Remind user to select at least one document
            MainWindowController.getMainWindowController().showDialogMessage("Veuillez sélecter au moins une demande de"
                    + " règlement\npour laquelle le document requi sera envoyé.", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    // </editor-fold>
}
