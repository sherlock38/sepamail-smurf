package smurf.utilities;

import com.itextpdf.awt.PdfGraphics2D;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.xml.xmp.DublinCoreSchema;
import com.itextpdf.text.xml.xmp.PdfA1Schema;
import com.itextpdf.text.xml.xmp.XmpBasicSchema;
import com.itextpdf.text.xml.xmp.XmpWriter;
import java.awt.print.PrinterException;
import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;
import org.jopendocument.dom.spreadsheet.Sheet;
import org.jopendocument.dom.spreadsheet.SpreadSheet;
import org.jopendocument.model.OpenDocument;
import org.jopendocument.renderer.ODTRenderer;
import smurf.Smurf;
import smurf.dao.ConfigurationDao;
import smurf.exceptions.*;
import smurf.model.AvisClient;
import smurf.model.Configuration;

/**
 * SEPAMailDocumentPrinter class is used to create the various documents required for a request for payment document
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 0.6
 */
public class SEPAMailDocumentPrinter {

    private ArrayList<Configuration> configurations;
    private Date documentGenerationDate;
    private File templateFile;
    private File xmlTemplateFile;
    private String outputFolderName;
    private SimpleDateFormat fileDateFormat;
    private String templateFilename;
    private String templateFilenameOnly;
    private String templateFolderName;
    private String temporaryFolderName;
    private String xmlTemplateFilename;
    private String xmlTemplateFilenameOnly;

    // <editor-fold defaultstate="collapsed" desc="Document printer constructor">
    /**
     * SEPAMailDocumentPrinter class constructor
     * 
     * @throws IOException
     * @throws ConfigurationFormatException
     * @throws InvalidTemplatePathException
     * @throws RequestForPaymentTemplateNotFoundException 
     */
    public SEPAMailDocumentPrinter() throws IOException, ConfigurationFormatException, InvalidTemplatePathException,
            RequestForPaymentTemplateNotFoundException, RequestForPaymentTemplateNotDefinedException,
            SepaMailTemplateNotDefinedException, SepaMailTemplateNotFoundException {

        // Initialise class attributes
        this.configurations = ConfigurationDao.getConfigurationDao().getConfigurations();
        this.documentGenerationDate = new Date();
        this.fileDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

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

        // Get ouput folder name
        int outputFolderIndex = this.configurations.indexOf(new Configuration("folder.output"));
        if (outputFolderIndex > -1) {
            this.outputFolderName = this.configurations.get(outputFolderIndex).getStringVal();
        } else {
            this.outputFolderName = "./output";
        }

        // Create output foler if it does not exist
        Utilities.createFolderIfNotExist(Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator")
                + this.outputFolderName);

        // Get request for payment SEPAmail XML document filename
        int sepaMailTemplateFilenameIndex = this.configurations.indexOf(new Configuration("template.sepamail"));
        if (sepaMailTemplateFilenameIndex > -1) {
            this.xmlTemplateFilename = this.configurations.get(sepaMailTemplateFilenameIndex).getStringVal();
        } else {
            throw new SepaMailTemplateNotDefinedException();
        }

        // Get request for payment template filename
        int templateFilenameIndex = this.configurations.indexOf(new Configuration("template.request"));
        if (templateFilenameIndex > -1) {
            this.templateFilename = this.configurations.get(templateFilenameIndex).getStringVal();
        } else {
            throw new RequestForPaymentTemplateNotDefinedException();
        }

        // Template folder
        File templateFolder = new File(Utilities.getCurrentWorkingDirectory()
                + System.getProperty("file.separator") + this.templateFolderName);

        // Check if the template path appeares to be valid
        if ((!templateFolder.exists()) && (!templateFolder.isDirectory())) {
            throw new InvalidTemplatePathException(Utilities.getCurrentWorkingDirectory()
                + System.getProperty("file.separator") + this.templateFolderName);
        }

        // SEPAmail XML file
        this.xmlTemplateFile = new File(Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator")
                + this.templateFolderName + System.getProperty("file.separator") + this.xmlTemplateFilename);

        // Check if the SEPAmail XML document template file was found otherwise throw exception
        if (!this.xmlTemplateFile.exists()) {
            throw new SepaMailTemplateNotFoundException(Utilities.getCurrentWorkingDirectory()
                + System.getProperty("file.separator") + this.templateFolderName);
        }

        // Check if the XML template file is not a directory otherwise throw exception
        if (this.xmlTemplateFile.isDirectory()) {
            throw new SepaMailTemplateNotFoundException(Utilities.getCurrentWorkingDirectory()
                + System.getProperty("file.separator") + this.templateFolderName);
        }

        // Template file
        this.templateFile = new File(Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator")
                + this.templateFolderName + System.getProperty("file.separator") + this.templateFilename);

        // Check if a template file was found otherwise throw exception
        if (!this.templateFile.exists()) {
            throw new RequestForPaymentTemplateNotFoundException(Utilities.getCurrentWorkingDirectory()
                + System.getProperty("file.separator") + this.templateFolderName);
        }

        // Check if the template file is not a directory otherwise throw exception
        if (this.templateFile.isDirectory()) {
            throw new RequestForPaymentTemplateNotFoundException(Utilities.getCurrentWorkingDirectory()
                + System.getProperty("file.separator") + this.templateFolderName);
        }

        // Template file name without extension
        this.templateFilenameOnly = this.templateFilename.substring(0, this.templateFilename.length() - 4);

        // XML template file name without extension
        this.xmlTemplateFilenameOnly = this.xmlTemplateFilename.substring(0, this.xmlTemplateFilename.length() - 4);

    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Generate document for a given request for payment object">
    /**
     * Generate the request for payment document
     * 
     * @param avisClient Request for payment object
     * @return Path and name of the request for payment document
     * @throws IOException
     */
    public String generateRequestForPaymentDocument(AvisClient avisClient) throws IOException, FileNotFoundException,
            DocumentException, PrinterException {

        // Generate the temporary ODS file for the given request for payment
        String tempOdsFile = this.createODSOutput(avisClient);

        // Generate the SEPAmail missive XML file for the given request for payment
        String sepaMailXmlFile = this.generateSepaMailXml(avisClient);

        // Generate the request for payment document and return the path and file name of the document
        return this.createOutputPdf(avisClient, tempOdsFile, sepaMailXmlFile);

    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Generate ODS file using template and request for payment data">
    /**
     * Create an ODS file with values replaced for a given request for payment using the request for payment template
     * 
     * @param avisClient Request for payment object
     * @return Path and name of the temporary ODS file generated
     */
    private String createODSOutput(AvisClient avisClient) throws IOException {

        // Create a map of tokens and corresponding values
        HashMap<String, String> tokens = new HashMap<String, String>();
        
        // Add tokens and corresponding values to the map
        try {
            tokens.put("#SMURF#date_avis#", avisClient.getFormattedAttribute("date_avis"));
        } catch (RequestForPaymentAttributeNotFoundException ex) {
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                    ex.getLocalizedMessage());
            tokens.put("#SMURF#date_avis#", "#NA");
        }
        
        try {
            tokens.put("#SMURF#client#", avisClient.getFormattedAttribute("client"));
        } catch (RequestForPaymentAttributeNotFoundException ex) {
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                    ex.getLocalizedMessage());
            tokens.put("#SMURF#client#", "#NA");
        }
        
        try {
            tokens.put("#SMURF#identifiant_client#", avisClient.getFormattedAttribute("identifiant_client"));
        } catch (RequestForPaymentAttributeNotFoundException ex) {
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                    ex.getLocalizedMessage());
            tokens.put("#SMURF#identifiant_client#", "#NA");
        }
        
        try {
            tokens.put("#SMURF#montant_total#", avisClient.getFormattedAttribute("montant_total"));
        } catch (RequestForPaymentAttributeNotFoundException ex) {
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                    ex.getLocalizedMessage());
            tokens.put("#SMURF#montant_total#", "#NA");
        }
        
        try {
            tokens.put("#SMURF#date_paiement#", avisClient.getFormattedAttribute("date_paiement"));
        } catch (RequestForPaymentAttributeNotFoundException ex) {
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                    ex.getLocalizedMessage());
            tokens.put("#SMURF#date_paiement#", "#NA");
        }

        // Read the template file
        Sheet templateSheet = SpreadSheet.createFromFile(this.templateFile).getSheet(0);

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

                        // Array of keys that have been used to replace values in the template
                        ArrayList<String> matchedKeys = new ArrayList<String>();

                        // Tokens map iterator
                        Iterator mapIt = tokens.entrySet().iterator();

                        // Iterate through the set of keys and replace with corresponding values
                        while (mapIt.hasNext()) {

                            // Current map entry
                            Map.Entry<String, String> entry = (Map.Entry<String, String>)mapIt.next();

                            // Check if the cell contains the current map entry key
                            if (StringUtils.countMatches(cellContent, entry.getKey()) > 0) {

                                // Replace cell content
                                cellContent = cellContent.replaceAll(entry.getKey(), entry.getValue());

                                // Add the key to the array of matched keys
                                matchedKeys.add(entry.getKey());

                                // Avoids a ConcurrentModificationException
                                mapIt.remove();

                                // Check if we need to exit the iterator loop
                                if (matchedKeys.size() >= matches) {
                                    break;
                                }
                            }
                        }

                        // Remove keys for which values have already been replaced from tokens map
                        for (int j = 0; j < matchedKeys.size(); j++) {
                            tokens.remove(matchedKeys.get(j));
                        }

                        // Set cell content
                        templateSheet.setValueAt(cellContent, x, y);
                    }
                }
            }
        }

        // Temporary ODS output file name
        String odsOutFilename = Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator")
                + this.temporaryFolderName + System.getProperty("file.separator") + this.templateFilenameOnly + "_"
                + this.fileDateFormat.format(this.documentGenerationDate) + "_" + avisClient.getIdAvis().toString()
                + ".ods";

        // Temporary output file
        File outputTemplate = new File(odsOutFilename);
        
        // Save the temporary template file for the current request for payment
        templateSheet.getSpreadSheet().saveAs(outputTemplate);

        return odsOutFilename;

    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Generate SEPAmail missive XML document">
    /**
     * Generate the SEPAmail XML document that needs to be embedded into the request for payment PDF document
     * 
     * @param avisClient Request for payment object
     * @return SEPAmail XML document file name
     */
    private String generateSepaMailXml(AvisClient avisClient) throws FileNotFoundException, IOException {

        // ISO date/time formatter
        SimpleDateFormat isoDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        isoDateTimeFormat.setLenient(false);
        isoDateTimeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        // ISO date only formatter
        SimpleDateFormat isoDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        // Create a map of tokens and corresponding values
        HashMap<String, String> tokens = new HashMap<String, String>();

        // SEPAmail XML output filename
        String xmlOutFilename = Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator")
                + this.temporaryFolderName + System.getProperty("file.separator") + this.xmlTemplateFilenameOnly + "_"
                + this.fileDateFormat.format(this.documentGenerationDate) + "_" + avisClient.getIdAvis().toString()
                + ".xml";

        // Add tokens and corresponding values to the map
        try {
            tokens.put("#SMURF#MissiveID#", avisClient.getFormattedAttribute("identifiant_missive"));
        } catch (RequestForPaymentAttributeNotFoundException ex) {
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                    ex.getLocalizedMessage());
            tokens.put("#SMURF#MissiveID#", "#NA");
        }
        
        try {

            // Get payment reminder date
            Object dateAvis = avisClient.getAttribute("date_avis");

            // Check the payment reminder date object instance
            if (dateAvis.getClass().getName().equals("java.sql.Date")) {

                // Convert date to ISO date
                tokens.put("#SMURF#date_avis_iso#",
                        isoDateTimeFormat.format((Date)avisClient.getAttribute("date_avis")));
            } else {

                // Date parser instance
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");

                try {

                    // Convert date to Date object
                    Date date = dateFormat.parse(dateAvis.toString());

                    // Convert date to ISO date
                    tokens.put("#SMURF#date_avis_iso#", isoDateTimeFormat.format(date));

                } catch (ParseException ex) {
                    Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                            ex.getLocalizedMessage());
                    tokens.put("#SMURF#date_avis_iso#", "#NA");
                }
            }

        } catch (RequestForPaymentAttributeNotFoundException ex) {
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                    ex.getLocalizedMessage());
            tokens.put("#SMURF#date_avis_iso#", "#NA");
        }
        
        try {
            tokens.put("#SMURF#client#BIC#", avisClient.getFormattedAttribute("client_bic"));
        } catch (RequestForPaymentAttributeNotFoundException ex) {
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                    ex.getLocalizedMessage());
            tokens.put("#SMURF#client#BIC#", "#NA");
        }
        
        try {
            tokens.put("#SMURF#client#IBAN#", avisClient.getFormattedAttribute("client_iban"));
        } catch (RequestForPaymentAttributeNotFoundException ex) {
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                    ex.getLocalizedMessage());
            tokens.put("#SMURF#client#IBAN#", "#NA");
        }
        
        try {

            // Get the payment date object
            Object datePaiement = avisClient.getAttribute("date_paiement");

            // Check the payment date object instance
            if (datePaiement.getClass().getName().equals("java.sql.Date")) {
                tokens.put("#SMURF#date_paiement#",
                        isoDateFormat.format((Date)avisClient.getAttribute("date_paiement")));
            } else {

                // Date parser instance
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");

                try {

                    // Convert date to Date object
                    Date date = dateFormat.parse(datePaiement.toString());

                    // Convert date to ISO date
                    tokens.put("#SMURF#date_paiement#", isoDateTimeFormat.format(date));

                } catch (ParseException ex) {
                    Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                            ex.getLocalizedMessage());
                    tokens.put("#SMURF#date_paiement#", "#NA");
                }
            }

        } catch (RequestForPaymentAttributeNotFoundException ex) {
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                    ex.getLocalizedMessage());
            tokens.put("#SMURF#date_paiement#", "#NA");
        }
        
        try {
            tokens.put("#SMURF#client#", avisClient.getFormattedAttribute("client"));
        } catch (RequestForPaymentAttributeNotFoundException ex) {
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                    ex.getLocalizedMessage());
            tokens.put("#SMURF#client#", "#NA");
        }
        
        try {
            tokens.put("#SMURF#montant_total#", avisClient.getAttribute("montant_total").toString());
        } catch (RequestForPaymentAttributeNotFoundException ex) {
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                    ex.getLocalizedMessage());
            tokens.put("#SMURF#montant_total#", "#NA");
        }

        String line;

        // Buffered read
        BufferedReader br = new BufferedReader(new FileReader(this.xmlTemplateFile));

        // File writer stream
        FileWriter fw = new FileWriter(xmlOutFilename);

        // Buffered writer
        BufferedWriter bw = new BufferedWriter(fw);

        // Read the file line by line
        while((line = br.readLine()) != null) {

            // Check the number of time the #SMURF# keyword can be found in a line
            int matches = StringUtils.countMatches(line, "#SMURF#");

            // Check if the current line contains the #SMURF# keyword
            if (matches > 0) {

                // #SMURF#client#BIC# special case
                int sp1Matches = StringUtils.countMatches(line, "#SMURF#client#BIC#");
                if (sp1Matches > 0) {
                    line = line.replaceAll("#SMURF#client#BIC#", tokens.get("#SMURF#client#BIC#"));
                    matches = matches - sp1Matches;
                }

                // #SMURF#client#IBAN# special case
                int sp2Matches = StringUtils.countMatches(line, "#SMURF#client#IBAN#");
                if (sp2Matches > 0) {
                    line = line.replaceAll("#SMURF#client#IBAN#", tokens.get("#SMURF#client#IBAN#"));
                    matches = matches - sp2Matches;
                }

                // We will check for the other tokens if we still have keyword matches
                if (matches > 0) {

                    // Tokens map iterator
                    Iterator mapIt = tokens.entrySet().iterator();

                    // Iterate through the set of keys and replace with corresponding values
                    while (mapIt.hasNext()) {

                        // Current map entry
                        Map.Entry<String, String> entry = (Map.Entry<String, String>)mapIt.next();

                        // Check if the LINE contains the current map entry key
                        if (StringUtils.countMatches(line, entry.getKey()) > 0) {

                            // Replace line content
                            line = line.replaceAll(entry.getKey(), entry.getValue());

                            // Decrease the number of matches
                            matches--;

                            // Check if we need to exit the iterator loop
                            if (matches < 1) {
                                break;
                            }
                        }
                    }
                }
            }

            // Write line to output file
            bw.write(line + System.getProperty("line.separator"));
        }

        // Close the input file reader
        br.close();

        // Close output file writer
        bw.close();

        return xmlOutFilename;

    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Generate PDFA1A request for payment document with required XMP tags">
    /**
     * Generate PDFA1A compliant request for payment document with appropriate XMP content
     * 
     * @param avisClient Request for payment object
     * @param odsFilename Path and file name of the ODS file
     * @param xmlFilename Path and file name of the SEPAmail XML missive file
     * @return Path and file name of the preliminary PDF
     */
    private String createOutputPdf(AvisClient avisClient, String odsFilename, String xmlFilename) throws
            FileNotFoundException, DocumentException, PrinterException, IOException {

        // Output filename
        String outputFilename = Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator")
                + this.outputFolderName + System.getProperty("file.separator") + this.templateFilenameOnly + "_"
                + this.fileDateFormat.format(this.documentGenerationDate) + "_" + avisClient.getIdAvis().toString()
                + ".pdf";

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
        writer.setPDFXConformance(PdfWriter.PDFA1A);
        writer.setTagged();

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

        // XMP schema for SEPAmail properties
        XmpBasicSchema cp = new XmpBasicSchema();

        // Encoded false
        String jFalse = "false";
        byte[] bFalse = jFalse.getBytes("UTF-8");

        // Encoded generator name
        String jGeneratorName = "smurf";
        byte[] bGeneratorName = jGeneratorName.getBytes("UTF-8");

        // SEPAmail XMP properties
        cp.setProperty("xmp:sepamail_missive", this.encodedSEPAmailMissiveXml(xmlFilename));
        cp.setProperty("xmp:sepamail_document.signed", new String(bFalse, "UTF-8"));
        cp.setProperty("xmp:sepamail_document.generator", new String(bGeneratorName, "UTF-8"));

        // Add SEPAmail data to XMP
        xmp.addRdfDescription(cp);

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
        File odsFile = new File(odsFilename);
        odsFile.delete();

        // Delete SEPAmail XML missive document that has already been embedded in the PDF
        File xmlFile = new File(xmlFilename);
        xmlFile.delete();

        return outputFilename;

    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="ODS content size">
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

    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="PDF document page size">
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
            Smurf.logController.log(Level.WARNING, SEPAMailDocumentPrinter.class.getSimpleName(),
                    ex.getLocalizedMessage());
        }

        return new Rectangle(594.72f, 280.8f);

    }// </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="UTF-8 encoded SEPAmail XML missive content">
    /**
     * Read the SEPAmail missive XML filename and encode content to UTF-8
     * 
     * @param xmlFilename SEPAmail missive XML file name
     * @return UTF-8 encoded string
     * @throws FileNotFoundException
     * @throws IOException
     */
    private String encodedSEPAmailMissiveXml(String xmlFilename) throws FileNotFoundException, IOException {

        // XML missive file
        File xmlFile = new File(xmlFilename);

        // XML file stream reader
        FileInputStream fis = new FileInputStream(xmlFile);

        // Byte array that will store content of the file
        byte[] content = new byte[(int)xmlFile.length()];

        // Get file content
        fis.read(content);

        // Close the file input stream
        fis.close();

        return new String(content, "UTF-8");

    }// </editor-fold>

}
