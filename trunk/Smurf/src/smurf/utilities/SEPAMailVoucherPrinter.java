package smurf.utilities;

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
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.jopendocument.model.OpenDocument;
import org.jopendocument.renderer.ODTRenderer;
import smurf.Smurf;
import smurf.dao.ConfigurationDao;
import smurf.exceptions.ConfigurationFormatException;
import smurf.exceptions.LogTemplateNotDefinedException;
import smurf.exceptions.LogTemplateNotFoundException;
import smurf.exceptions.RequestForPaymentAttributeNotFoundException;
import smurf.model.AvisClient;
import smurf.model.Configuration;

/**
 * SEPAMailVoucherPrinter class is used to create a sent documents voucher for SMURF
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class SEPAMailVoucherPrinter {

    private ArrayList<Configuration> configurations;
    private SimpleDateFormat fileDateFormat;
    private String logFolderName;
    private File templateFile;
    private String templateFilename;
    private String templateFilenameOnly;
    private String templateFolderName;
    private String temporaryFolderName;

    /**
     * SEPAMailVoucherPrinter default constructor
     * 
     * @throws IOException
     * @throws ConfigurationFormatException
     * @throws LogTemplateNotDefinedException
     * @throws LogTemplateNotFoundException 
     */
    public SEPAMailVoucherPrinter() throws IOException, ConfigurationFormatException, LogTemplateNotDefinedException,
            LogTemplateNotFoundException {

        // Initialise class attributes
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
    }

    /**
     * Generate the PDF voucher for the given list of request for payment objects
     * 
     * @param avisClients List of request for payment objects for which documents will be sent
     * @return PDF voucher file path and name
     * @throws IOException
     * @throws FileNotFoundException
     * @throws DocumentException
     * @throws PrinterException
     */
    public String generateVoucher(ArrayList<AvisClient> avisClients) throws IOException, FileNotFoundException,
            DocumentException, PrinterException {

        // ODS with list of request for payments
        String odsFilename = this.generateTemporaryOds(avisClients);

        // Generate PDF voucher and get name and path of the voucher file
        return this.createPdfLog(odsFilename);
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
     * Generate temporary ODS log
     * 
     * @param avisClients List of request for payment objects for which documents will be sent
     * @return Temporary ODS log filename
     * @throws IOException
     */
    private String generateTemporaryOds(ArrayList<AvisClient> avisClients) throws IOException {

        String[] date_avis = new String[avisClients.size()];
        String[] client = new String[avisClients.size()];
        String[] identifiant_client = new String[avisClients.size()];
        String[] montant_total = new String[avisClients.size()];

        // Build the values that must be added to the template
        for (int i = 0; i < avisClients.size(); i++) {

            // Current request for payment object
            AvisClient avisClient = avisClients.get(i);

            // Add tokens and corresponding values to the map
            try {
                date_avis[i] = avisClient.getFormattedAttribute("date_avis");
            } catch (RequestForPaymentAttributeNotFoundException ex) {
                Smurf.logController.log(Level.WARNING, SEPAMailVoucherPrinter.class.getSimpleName(),
                        ex.getLocalizedMessage());
                date_avis[i] = "#NA";
            }

            try {
                client[i] = avisClient.getFormattedAttribute("client");
            } catch (RequestForPaymentAttributeNotFoundException ex) {
                Smurf.logController.log(Level.WARNING, SEPAMailVoucherPrinter.class.getSimpleName(),
                        ex.getLocalizedMessage());
               client[i] = "#NA";
            }

            try {
                identifiant_client[i] = avisClient.getFormattedAttribute("identifiant_client");
            } catch (RequestForPaymentAttributeNotFoundException ex) {
                Smurf.logController.log(Level.WARNING, SEPAMailVoucherPrinter.class.getSimpleName(),
                        ex.getLocalizedMessage());
                identifiant_client[i] = "#NA";
            }

            try {
                montant_total[i] = avisClient.getFormattedAttribute("montant_total");
            } catch (RequestForPaymentAttributeNotFoundException ex) {
                Smurf.logController.log(Level.WARNING, SEPAMailVoucherPrinter.class.getSimpleName(),
                        ex.getLocalizedMessage());
                montant_total[i] = "#NA";
            }
        }

        // Read the template file
        Sheet templateSheet = SpreadSheet.createFromFile(this.templateFile).getSheet(0);

        // Set the row count of the template
        templateSheet.setRowCount(templateSheet.getRowCount() + avisClients.size() - 1);

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
                        switch (cellContent) {

                            case "#SMURF#date_avis#":

                                // Fill in the values for the #SMURF#date_avis# place holder
                                for (int z = 0; z < date_avis.length; z++) {
                                    templateSheet.setValueAt(date_avis[z], x, y + z);
                                }
                                break;

                            case "#SMURF#client#":

                                // Fill in the values for the #SMURF#client# place holder
                                for (int z = 0; z < client.length; z++) {
                                    templateSheet.setValueAt(client[z], x, y + z);
                                }
                                break;

                            case "#SMURF#identifiant_client#":

                                // Fill in the values for the #SMURF#identifiant_client# place holder
                                for (int z = 0; z < identifiant_client.length; z++) {
                                    templateSheet.setValueAt(identifiant_client[z], x, y + z);
                                }
                                break;

                            case "#SMURF#montant_paiement#":

                                // Fill in the values for the #SMURF#montant_paiement# place holder
                                for (int z = 0; z < montant_total.length; z++) {
                                    templateSheet.setValueAt(montant_total[z], x, y + z);
                                }
                                break;
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
            Smurf.logController.log(Level.WARNING, SEPAMailVoucherPrinter.class.getSimpleName(),
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
}
