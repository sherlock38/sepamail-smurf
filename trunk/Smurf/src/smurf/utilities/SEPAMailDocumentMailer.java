package smurf.utilities;

import smurf.exceptions.MailParameterNotDefinedException;
import smurf.exceptions.RequestForPaymentAttributeNotFoundException;
import smurf.exceptions.LogTemplateNotDefinedException;
import smurf.exceptions.LogTemplateNotFoundException;
import smurf.exceptions.ConfigurationFormatException;
import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.xml.xmp.DublinCoreSchema;
import com.itextpdf.text.xml.xmp.PdfA1Schema;
import com.itextpdf.text.xml.xmp.XmpWriter;
import java.awt.print.PrinterException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.jopendocument.model.OpenDocument;
import org.jopendocument.renderer.ODTRenderer;
import smurf.Smurf;
import smurf.dao.ConfigurationDao;
import smurf.model.AvisClient;
import smurf.model.Configuration;

/**
 * SEPAMailDocumentMailer class is used to email the request for payment document and generate the log document for the
 * sent document
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 0.6
 */
public class SEPAMailDocumentMailer {

    private ArrayList<Configuration> configurations;
    private ArrayList<AvisClient> avisClients;
    private File templateFile;
    private int port;
    private SimpleDateFormat archiveFileDateFormat;
    private SimpleDateFormat fileDateFormat;
    private String archiveFilename;
    private String archiveFolderName;
    private String logFolderName;
    private String host;
    private String name;
    private String password;
    private String recipient;
    private String sender;
    private String templateFilename;
    private String templateFilenameOnly;
    private String templateFolderName;
    private String temporaryFolderName;
    private String transferType;
    private String username;

    /**
     * SEPAMailDocumentMailer constructor
     * 
     * @throws IOException
     * @throws ConfigurationFormatException
     * @throws LogTemplateNotDefinedException
     * @throws LogTemplateNotFoundException
     * @throws MailParameterNotDefinedException
     */
    public SEPAMailDocumentMailer() throws IOException, ConfigurationFormatException, LogTemplateNotDefinedException,
            LogTemplateNotFoundException, MailParameterNotDefinedException {

        // Initialise class attributes
        this.archiveFileDateFormat = new SimpleDateFormat("yyyyMMdd");
        this.avisClients = new ArrayList<AvisClient>();
        this.configurations = ConfigurationDao.getConfigurationDao().getConfigurations();
        this.fileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        // Log folder name
        this.logFolderName = "log";

        // Create log foler if it does not exist
        Utilities.createFolderIfNotExist(Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator")
                + "log");

        // Get template folder name
        int templateFolderIndex = this.configurations.indexOf(new Configuration("folder.template"));
        if (templateFolderIndex > -1) {
            this.templateFolderName = this.configurations.get(templateFolderIndex).getStringVal();
        } else {
            this.templateFolderName = "./template";
        }

        // Get temporary folder name
        int temporaryFolderIndex = this.configurations.indexOf(new Configuration("folder.temp"));
        if (temporaryFolderIndex > -1) {
            this.temporaryFolderName = this.configurations.get(temporaryFolderIndex).getStringVal();
        } else {
            this.temporaryFolderName = "./temp";
        }

        // Create temporary folder if it does not exist
        Utilities.createFolderIfNotExist(Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator")
                + this.temporaryFolderName);

        // Get log template filename
        int templateFilenameIndex = this.configurations.indexOf(new Configuration("template.ack"));
        if (templateFilenameIndex > -1) {
            this.templateFilename = this.configurations.get(templateFilenameIndex).getStringVal();
        } else {
            throw new LogTemplateNotDefinedException();
        }

        // Template file
        this.templateFile = new File(Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator")
                + this.templateFolderName + System.getProperty("file.separator") + this.templateFilename);

        // Check if a template file was found otherwise throw exception
        if (!this.templateFile.exists()) {
            throw new LogTemplateNotFoundException(Utilities.getCurrentWorkingDirectory() 
                    + System.getProperty("file.separator") + this.templateFolderName);
        }

        // Check if the template file is not a directory otherwise throw exception
        if (this.templateFile.isDirectory()) {
            throw new LogTemplateNotFoundException(Utilities.getCurrentWorkingDirectory()
                + System.getProperty("file.separator") + this.templateFolderName);
        }

        // Template file name without extension
        this.templateFilenameOnly = this.templateFilename.substring(0, this.templateFilename.length() - 4);

        // Get parameters required for sending request for payment documents
        this.getMailParameters();
    }

    /**
     * Send request for payment document for the given request for payment object
     * 
     * @param avisClient Request for payment object
     * @throws IOException
     * @throws FileNotFoundException
     * @throws DocumentException
     * @throws PrinterException
     */
    public void sendDocumentForRequest(AvisClient avisClient) throws IOException, FileNotFoundException,
            DocumentException, PrinterException {

        // Check the requests for payment transfer method
        if (this.transferType.equals("mail")) {

            try {

                // Mail the document for the given request for payment
                this.mailAsAttachment(avisClient);

                // Add the mail to the list of emails sent
                this.avisClients.add(avisClient);

            } catch (MessagingException ex) {
                
                // Write error message to log file
                Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                        ex.getLocalizedMessage());
            }

        } else if (this.transferType.equals("archive")) {

            // Add the mail to the list of emails sent
            this.avisClients.add(avisClient);
        }
    }

    /**
     * Create mail sending log for a given request for payment
     * 
     * @throws IOException
     * @throws FileNotFoundException
     * @throws DocumentException
     * @throws PrinterException
     */
    public void createMailLog() throws IOException, FileNotFoundException, DocumentException,
            PrinterException {

        // Check if there are requests for payment that were successfully sent
        if (this.avisClients.size() > 0) {

            // Check the mail transfer type
            if (this.transferType.equals("mail")) {

                // Generate temporary ODS log file
                String odsFilename = this.generateTemporaryOds();

                // Generate PDF log file
                this.createPdfLog(odsFilename);

            } else if (this.transferType.equals("archive")) {

                // Generate temporary ODS log file
                String odsFilename = this.generateTemporaryOds();

                // Generate PDF log file
                String logFilename = this.createPdfLog(odsFilename);

                // Get parameters for generating requests for payment document archive
                this.getArchiveParameters();

                // Archive folder name
                String currentArchiveFolderName = this.archiveFilename + "_"
                        + this.archiveFileDateFormat.format(new Date());

                // Archive folder path and name
                String archiveFolderPathName = Utilities.getCurrentWorkingDirectory() +
                        System.getProperty("file.separator") + temporaryFolderName
                        + System.getProperty("file.separator") + currentArchiveFolderName;
 
                // Create archive folder
                Utilities.createFolderIfNotExist(archiveFolderPathName);

                // Archive folder name and path
                File archiveFolderPathNameObj = new File(archiveFolderPathName);

                // Copy PDF documents to archive folder
                for (int i = 0; i < this.avisClients.size(); i++) {

                    // Request for payment document
                    File pdf = new File(this.avisClients.get(i).getPdfFile());

                    // Request for payment document destination
                    File destination = new File(archiveFolderPathName + System.getProperty("file.separator")
                            + pdf.getName());

                    // Copy request for payment document
                    FileUtils.copyFile(pdf, destination);
                }

                // Log file
                File logFile = new File(logFilename);

                // Destination of log file
                File logDestination = new File(archiveFolderPathName + System.getProperty("file.separator")
                            + logFile.getName());

                // Copy log file to archive folder
                FileUtils.copyFile(logFile, logDestination);

                // Archive filename and path
                File archive = new File(Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator")
                        + this.archiveFolderName + System.getProperty("file.separator") + currentArchiveFolderName
                        + ".zip");

                // Create archive
                Utilities.createFolderZipArchive(archiveFolderPathNameObj, archive);

                // List of files in the archive folder
                String files[] = archiveFolderPathNameObj.list();

                // Delete the files in the temporary folder
                for (int i = 0; i < files.length; i++) {

                    // Current file
                    File currentFile = new File(archiveFolderPathNameObj.getAbsolutePath()
                            + System.getProperty("file.separator") + files[i]);

                    // Delete the file
                    currentFile.delete();
                }

                // Delete the temporary archive folder
                archiveFolderPathNameObj.delete();
            }
        }
    }

    /**
     * Get parameters required for sending request for payment documents
     * 
     * @throws MailParameterNotDefinedException 
     */
    private void getMailParameters() throws MailParameterNotDefinedException, ConfigurationFormatException {

        // Get mail transfer type
        int transferTypeIndex = this.configurations.indexOf(new Configuration("mail.type"));
        if (transferTypeIndex > -1) {

            // Get parameter value
            this.transferType = this.configurations.get(transferTypeIndex).getStringVal();

            // Check the mail transfer type value
            if (!(this.transferType.equals("mail") || this.transferType.equals("archive"))) {

                // Set the default mail transfer type
                this.transferType = "mail";
            }

        } else {

            // Set the default mail transfer type
            this.transferType = "mail";
        }

        // Get mail SMTP host
        int hostIndex = this.configurations.indexOf(new Configuration("mail.smtp"));
        if (hostIndex > -1) {

            // Get parameter value
            this.host = this.configurations.get(hostIndex).getStringVal();
            
            // Check that a value has been defined for the SMTP host
            if (this.host.length() < 1) {
                throw new MailParameterNotDefinedException("mail.smtp");
            }
        } else {
            throw new MailParameterNotDefinedException("mail.smtp");
        }

        // Get mail sender name
        int nameIndex = this.configurations.indexOf(new Configuration("mail.name"));
        if (nameIndex > -1) {

            // Get parameter value
            this.name = this.configurations.get(nameIndex).getStringVal();
            
            // Check that a value has been defined for the mail sender name
            if (this.name.length() < 1) {
                throw new MailParameterNotDefinedException("mail.name");
            }
        } else {
            throw new MailParameterNotDefinedException("mail.name");
        }

        // Get mail sender address
        int senderIndex = this.configurations.indexOf(new Configuration("mail.sender"));
        if (senderIndex > -1) {

            // Get parameter value
            this.sender = this.configurations.get(senderIndex).getStringVal();
            
            // Check that a value has been defined for the sender address
            if (this.sender.length() < 1) {
                throw new MailParameterNotDefinedException("mail.sender");
            }
        } else {
            throw new MailParameterNotDefinedException("mail.sender");
        }

        // Get mail recipient address
        int recipientIndex = this.configurations.indexOf(new Configuration("mail.recipient"));
        if (recipientIndex > -1) {

            // Get parameter value
            this.recipient = this.configurations.get(recipientIndex).getStringVal();
            
            // Check that a value has been defined for the recipient address
            if (this.recipient.length() < 1) {
                throw new MailParameterNotDefinedException("mail.recipient");
            }
        } else {
            throw new MailParameterNotDefinedException("mail.recipient");
        }

        // Get mail SMTP host username
        int usernameIndex = this.configurations.indexOf(new Configuration("mail.user"));
        if (usernameIndex > -1) {

            // Get parameter value
            this.username = this.configurations.get(usernameIndex).getStringVal();
            
            // Check that a value has been defined for the SMTP host username
            if (this.username.length() < 1) {
                throw new MailParameterNotDefinedException("mail.user");
            }
        } else {
            throw new MailParameterNotDefinedException("mail.user");
        }

        // Get mail SMTP host password
        int passwordIndex = this.configurations.indexOf(new Configuration("mail.password"));
        if (passwordIndex > -1) {

            // Get parameter value
            this.password = this.configurations.get(passwordIndex).getStringVal();
            
            // Check that a value has been defined for the SMTP host password
            if (this.password.length() < 1) {
                throw new MailParameterNotDefinedException("mail.password");
            }
        } else {
            throw new MailParameterNotDefinedException("mail.password");
        }

        // Get mail SMTP port
        int portIndex = this.configurations.indexOf(new Configuration("mail.port"));
        if (portIndex > -1) {

            // Get parameter value
            this.port = this.configurations.get(portIndex).getIntVal();
            
            // Check that a value has been defined for the SMTP port
            if (this.port < 1) {
                throw new MailParameterNotDefinedException("mail.port");
            }
        } else {
            throw new MailParameterNotDefinedException("mail.port");
        }
    }

    /**
     * Send document for given request for payment object via mail
     * 
     * @param avisClient Request for payment object
     * @throws MessagingException
     */
    private void mailAsAttachment(AvisClient avisClient) throws MessagingException {

        // Authenticator for SMTP host
        SmurfAuthenticator smurfAuthenticator = new SmurfAuthenticator(this.username, this.password);

        // Mailer properties
        Properties properties = new Properties();
        properties.setProperty("mail.smtp.submitter", smurfAuthenticator.getPasswordAuthentication().getUserName());
        properties.setProperty("mail.smtp.auth", "true");
        properties.setProperty("mail.smtp.host", this.host);
        properties.setProperty("mail.smtp.port", String.valueOf(this.port));

        // Mailer session
        Session session = Session.getInstance(properties, smurfAuthenticator);

        // Create mail message
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(this.sender));
        InternetAddress[] address = {new InternetAddress(this.recipient)};
        msg.setRecipients(Message.RecipientType.TO, address);
        msg.setSubject("Avis de paiement SEPAmail");

        // Create and fill the first message part
        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setText("Avis de paiement SEPAmail");

        // Create the second message part
        MimeBodyPart mbp2 = new MimeBodyPart();

        // Attach the file to the message
        FileDataSource fds = new FileDataSource(avisClient.getPdfFile());
        mbp2.setDataHandler(new DataHandler(fds));
        mbp2.setFileName(fds.getName());

        // Create the Multipart and add its parts to it
        Multipart mp = new MimeMultipart();
        mp.addBodyPart(mbp1);
        mp.addBodyPart(mbp2);

        // Add the Multipart to the message
        msg.setContent(mp);

        // Set the Date: header
        msg.setSentDate(new Date());
      
        // Send the message
        Transport.send(msg);
    }

    /**
     * Generate temporary ODS log
     * 
     * @return Temporary ODS log filename
     * @throws IOException
     */
    private String generateTemporaryOds() throws IOException {

        String[] date_avis = new String[this.avisClients.size()];
        String[] client = new String[this.avisClients.size()];
        String[] identifiant_client = new String[this.avisClients.size()];
        String[] montant_total = new String[this.avisClients.size()];

        // Build the values that must be added to the template
        for (int i = 0; i < this.avisClients.size(); i++) {

            // Current request for payment object
            AvisClient avisClient = this.avisClients.get(i);

            // Add tokens and corresponding values to the map
            try {
                date_avis[i] = avisClient.getFormattedAttribute("date_avis");
            } catch (RequestForPaymentAttributeNotFoundException ex) {
                Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                        ex.getLocalizedMessage());
                date_avis[i] = "#NA";
            }

            try {
                client[i] = avisClient.getFormattedAttribute("client");
            } catch (RequestForPaymentAttributeNotFoundException ex) {
                Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                        ex.getLocalizedMessage());
               client[i] = "#NA";
            }

            try {
                identifiant_client[i] = avisClient.getFormattedAttribute("identifiant_client");
            } catch (RequestForPaymentAttributeNotFoundException ex) {
                Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                        ex.getLocalizedMessage());
                identifiant_client[i] = "#NA";
            }

            try {
                montant_total[i] = avisClient.getFormattedAttribute("montant_total");
            } catch (RequestForPaymentAttributeNotFoundException ex) {
                Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                        ex.getLocalizedMessage());
                montant_total[i] = "#NA";
            }
        }

        // Read the template file
        Sheet templateSheet = SpreadSheet.createFromFile(this.templateFile).getSheet(0);

        // Set the row count of the template
        templateSheet.setRowCount(templateSheet.getRowCount() + this.avisClients.size() - 1);

        // Scan the rows and columns of the sheet
        for (int x = 0; x < templateSheet.getColumnCount(); x++) {
            for (int y = 0; y < templateSheet.getRowCount(); y++) {

                // Check if the cell designated by coordinates x and y is valid
                if (templateSheet.isCellValid(x, y)) {

                    // Content of cell
                    String cellContent = templateSheet.getCellAt(x, y).getTextValue();

                    // Number of times the #SMURF# keyword occurs in the cell content
                    int matches = StringUtils.countMatches(cellContent, "#SMURF#");

                    // Check if the content of the cell contains the #SMURF# keyword
                    if (matches > 0) {

                        // Check the cell content
                        if (cellContent.equals("#SMURF#date_avis#")) {

                            // Fill in the values for the #SMURF#date_avis# place holder
                            for (int z = 0; z < date_avis.length; z++) {
                                templateSheet.setValueAt(date_avis[z], x, y + z);
                            }

                        } else if (cellContent.equals("#SMURF#client#")) {

                            // Fill in the values for the #SMURF#client# place holder
                            for (int z = 0; z < client.length; z++) {
                                templateSheet.setValueAt(client[z], x, y + z);
                            }

                        } else if (cellContent.equals("#SMURF#identifiant_client#")) {

                            // Fill in the values for the #SMURF#identifiant_client# place holder
                            for (int z = 0; z < identifiant_client.length; z++) {
                                templateSheet.setValueAt(identifiant_client[z], x, y + z);
                            }

                        } else if (cellContent.equals("#SMURF#montant_paiement#")) {

                            // Fill in the values for the #SMURF#montant_paiement# place holder
                            for (int z = 0; z < montant_total.length; z++) {
                                templateSheet.setValueAt(montant_total[z], x, y + z);
                            }

                        }
                    }
                }
            }
        }

        // Temporary ODS output file name
        String odsOutFilename = Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator")
                + this.temporaryFolderName + System.getProperty("file.separator") + this.templateFilenameOnly + "_"
                + this.fileDateFormat.format(new Date()) + ".ods";

        // Temporary output file
        File outputTemplate = new File(odsOutFilename);

        // Save the temporary template file for the current request for payment
        templateSheet.getSpreadSheet().saveAs(outputTemplate);

        return odsOutFilename;
    }

    /**
     * Generate PDFA1A compliant log file
     * 
     * @param odsFilename Path and file name of the ODS file
     * @return Log file name
     * @throws FileNotFoundException
     * @throws DocumentException
     * @throws PrinterException
     * @throws IOException
     */
    private String createPdfLog(String odsFilename) throws FileNotFoundException, DocumentException, PrinterException,
            IOException {

        // ODS file
        File odsFile = new File(odsFilename);

        // Output filename
        String outputFilename = Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator")
                + this.logFolderName + System.getProperty("file.separator")
                + odsFile.getName().substring(0, odsFile.getName().length() - 4) + ".pdf";

        // Open the ODS file
        OpenDocument doc = new OpenDocument();
        doc.loadFrom(odsFilename);
        
        // ODS file renderer
        ODTRenderer odtRenderer = new ODTRenderer(doc);
        odtRenderer.setIgnoreMargins(true);
        odtRenderer.setPaintMaxResolution(true);

        // PDF document size
        Rectangle pageSize = this.getPageSize();

        // Create PDF document using the print size of the ODS document
        Document document = new Document(pageSize);
        File outFile = new File(outputFilename);

        // PDF document content writer
        FileOutputStream fileOutputStream = new FileOutputStream(outFile);
        PdfWriter writer = PdfWriter.getInstance(document, fileOutputStream);

        // PDF document version
        writer.setPdfVersion(PdfWriter.PDF_VERSION_1_5);
        writer.setTagged();
        writer.setPDFXConformance(PdfWriter.PDFA1A);

        // Content size
        Rectangle contentSize = this.getOdsContentSize(odtRenderer);

        // Open the PDF document
        document.open();
        
        // PDF document tags
        document.addAuthor("smurf");
        document.addCreator("smurf");
        document.addCreationDate();
        document.addKeywords("");
        document.addProducer();
        document.addSubject("");
        document.addTitle("Avis de paiement SEPAmail");

        // Set the colour profile of the document
        PdfDictionary outputIntent = new PdfDictionary(PdfName.OUTPUTINTENT);
        
        // Colour profile dictionary properties
        outputIntent.put(PdfName.OUTPUTCONDITIONIDENTIFIER, new PdfString("sRGB IEC61966-2.1"));
        outputIntent.put(PdfName.INFO, new PdfString("sRGB IEC61966-2.1"));
        outputIntent.put(PdfName.S, PdfName.GTS_PDFA1);

        // Load the PDF document ICC profile
        ICC_Profile icc = ICC_Profile.getInstance(new FileInputStream(Utilities.getCurrentWorkingDirectory() 
                + System.getProperty("file.separator") + "icc" + System.getProperty("file.separator") + "srgb.icc"));

        // PDF ICC profile
        PdfICCBased pdfIcc = new PdfICCBased(icc);
        pdfIcc.remove(PdfName.ALTERNATE);
        
        // Add profile to PDF document
        outputIntent.put(PdfName.DESTOUTPUTPROFILE, writer.addToBody(pdfIcc).getIndirectReference()); 
        writer.getExtraCatalog().put(PdfName.OUTPUTINTENTS, new PdfArray(outputIntent));

        // Get a handle to PDF document content
        PdfContentByte contentByte = writer.getDirectContent();

        // PDF template
        PdfTemplate template = contentByte.createTemplate(contentSize.getWidth(), contentSize.getHeight());

        // Get graphics context to draw on the template
        PdfGraphics2D pdfGraphics2d = new PdfGraphics2D(template, contentSize.getWidth(), contentSize.getHeight());
        
        // Set document fonts
        pdfGraphics2d.setBoldFont(BaseFont.createFont(Utilities.getCurrentWorkingDirectory() 
                + System.getProperty("file.separator") + "font" + System.getProperty("file.separator") + "arial_b.ttf",
                BaseFont.WINANSI, BaseFont.EMBEDDED));
        pdfGraphics2d.setNormalFont(BaseFont.createFont(Utilities.getCurrentWorkingDirectory() 
                + System.getProperty("file.separator") + "font" + System.getProperty("file.separator") + "arial.ttf",
                BaseFont.WINANSI, BaseFont.EMBEDDED));
        
        // Render ODS file content on the template graphics context
        odtRenderer.paintComponent(pdfGraphics2d);

        // Dispose graphics context to free memory
        pdfGraphics2d.dispose();

        // Calculate offsets so as to center content on PDF
        float offsetX = (pageSize.getWidth() - contentSize.getWidth()) / 2;
        float offsetY = (pageSize.getHeight() - contentSize.getHeight()) / 2;

        // Add the template to the PDF document and center the content
        contentByte.addTemplate(template, offsetX, offsetY);

        // Byte array output stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
       
        // XMP data writer
        XmpWriter xmp = new XmpWriter(baos);

        // XMP DublinCore schema
        DublinCoreSchema dcs = new DublinCoreSchema();

        // DublinCore schema properties
        dcs.addAuthor("smurf");
        dcs.addDescription("");
        dcs.addPublisher("smurf");
        dcs.addSubject("");
        dcs.addTitle("Avis de paiement SEPAmail");

        // Add DublinCore data to XMP
        xmp.addRdfDescription(dcs);

        // XMP schema for PDF conformance
        PdfA1Schema cs = new PdfA1Schema();

        // Set conformance schema properties
        cs.addConformance("A");

        // Add conformance schema to PDF
        xmp.addRdfDescription(cs);

        // Close XMP writer
        xmp.close();

        // Add XMP data to the PDF file
        writer.setXmpMetadata(baos.toByteArray());

        // Close the PDF document
        document.close();

        // Delete temporary ODS file used to generate the request for payment document
        odsFile.delete();

        return outputFilename;
    }
    
    /**
     * Get the PDF document page size
     * 
     * @return PDF document page size
     */
    private Rectangle getPageSize() {

        float documentWidth, documentHeight;
 
        try {

            // Try to obtain the document width
            int documentWidthIndex = this.configurations.indexOf(new Configuration("pdf.width"));
            if (documentWidthIndex > -1) {
                documentWidth = this.configurations.get(documentWidthIndex).getFloatVal();
            } else {
                documentWidth = 0f;
            }

            // Try to obtain the document height
            int documentHeightIndex = this.configurations.indexOf(new Configuration("pdf.height"));
            if (documentHeightIndex > -1) {
                documentHeight = this.configurations.get(documentHeightIndex).getFloatVal();
            } else {
                documentHeight = 0f;
            }

            // Check if we have valid width and height
            if ((documentHeight > 0f) && (documentWidth > 0f)) {

                // User defined page size
                return new Rectangle((float)((documentWidth / 2.54) * 72), (float)((documentHeight / 2.54) * 72));
            }

        } catch (ConfigurationFormatException ex) {

            // Write error message to log file
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                    ex.getLocalizedMessage());
        }

        return new Rectangle(594.72f, 280.8f);

    }

    /**
     * Get the size of the PDF document that will be generated
     * 
     * @param odtRenderer ODT renderer containing ODS file details
     * @return Rectangle which defines the print size of the PDF document
     */
    private Rectangle getOdsContentSize(ODTRenderer odtRenderer) {

        // Document size in points
        return new Rectangle((float)((odtRenderer.getPrintWidth() / 100000.0) * 2.54 * 72),
                (float)((odtRenderer.getPrintHeight() / 100000.0) * 2.54 * 72));

    }

    /**
     * Get parameters for generating an archive folder containing documents of requests for payment
     */
    private void getArchiveParameters() {

        // Archive folder name
        int archiveFolderNameIndex = this.configurations.indexOf(new Configuration("folder.archive"));
        if (archiveFolderNameIndex > -1) {

            // Get parameter value
            try {

                this.archiveFolderName = this.configurations.get(archiveFolderNameIndex).getStringVal();

                // Check if the folder name is valid
                if (this.archiveFolderName.length() < 1) {

                    // Write error message to log file
                    Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                            "The name of the folder for requests for payment document archives is not valid."); 

                    // Set the default archive folder name
                    this.archiveFolderName = "archive";

                }

            } catch (ConfigurationFormatException ex) {

                // Write error message to log file
                Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                        "The name of the folder for requests for payment document archives has not been defined.");

                // Set the default archive folder name
                this.archiveFolderName = "archive";

            }

        } else {

            // Set the default archive folder name
            this.archiveFolderName = "archive";

            // Write error message to log file
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                    "The name of the folder for requests for payment document archives has not been defined."); 

        }

        // Create the archive folder if it does not exist
        Utilities.createFolderIfNotExist(Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator")
                + this.archiveFolderName);

        // Archive file name
        int archiveFilenameIndex = this.configurations.indexOf(new Configuration("mail.archive"));
        if (archiveFilenameIndex > -1) {

            // Get parameter value
            try {

                this.archiveFilename = this.configurations.get(archiveFilenameIndex).getStringVal();

                // Check if the filename is valid
                if (this.archiveFilename.length() < 1) {

                    // Write error message to log file
                    Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                            "The name of the archive for requests for payment document is not valid."); 

                    // Set the default archive filename
                    this.archiveFilename = "archive_des_demandes_de_règlement";

                }

            } catch (ConfigurationFormatException ex) {

                // Write error message to log file
                Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                        "The name of the archive for requests for payment document is not valid."); 

                // Set the default archive filename
                this.archiveFilename = "archive_des_demandes_de_règlement";
            }

        } else {

            // Set the default archive filename
            this.archiveFilename = "archive_des_demandes_de_règlement";

            // Write error message to log file
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentMailer.class.getSimpleName(),
                    "The name of the requests for payment document archive has not been defined."); 

        }
    }
}
