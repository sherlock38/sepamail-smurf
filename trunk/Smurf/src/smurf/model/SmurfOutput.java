package smurf.model;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import org.smic.Smic;
import org.smic.exceptions.*;
import org.xml.sax.SAXException;
import smurf.utilities.Utilities;

/**
 * The SmurfOutput object represent a SMURF output document and provide means of obtaining a request for payment
 * document in PDF or missive XML format.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 0.1
 */
public class SmurfOutput {

    private String baseFilename;
    private String baseOutputFormat;
    private String outputFolder;
    private String secondaryFilename;
    private String smicConfigurationFileName;
    private String tempFolder;

    /**
     * Get the path and name of the request for payment document in the configured output format
     * 
     * @return Path and name of the request for payment document in the configured output format
     */
    public String getBaseFilename() {
        return this.baseFilename;
    }

    /**
     * Get the base output format
     * 
     * @return Base output format
     */
    public String getBaseOutputFormat() {
        return this.baseOutputFormat;
    }

    /**
     * Get the path and name of the request for payment document in the converted format
     * 
     * @return Path and name of the request for payment document in the converted format
     */
    public String getSecondaryFilename() {
        return this.secondaryFilename;
    }

    /**
     * SmurfOutput class constructor
     * 
     * @param baseFilename Path and name of the request for payment document in the configured output format
     * @param baseOuputFormat Base output format
     * @param outputFolder Path and name of document output folder
     * @param tempFolder Path and name of temporary files folder
     * @param smicConfigurationFileName SMIC module configuration file path and name
     * @throws ConfigurationFileNotFoundException
     * @throws IOException
     * @throws InvalidConfigurationException
     * @throws FileNotFoundException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws MissiveXmlNotFoundException
     * @throws UnsupportedEncodingException
     * @throws InvalidDocumentObjectTemplateException
     * @throws TransformerConfigurationException
     * @throws TransformerException
     * @throws XmlDocumentNotGeneratedException
     * @throws XPathExpressionException
     * @throws ContainerNodeNotFoundException
     * @throws InvalidNamespaceDefinitionException
     */
    public SmurfOutput(String baseFilename, String baseOuputFormat, String outputFolder, String tempFolder,
            String smicConfigurationFileName) throws ConfigurationFileNotFoundException, IOException,
            InvalidConfigurationException, FileNotFoundException, ParserConfigurationException, SAXException,
            MissiveXmlNotFoundException, UnsupportedEncodingException, InvalidDocumentObjectTemplateException,
            TransformerConfigurationException, TransformerException, XmlDocumentNotGeneratedException,
            XPathExpressionException, ContainerNodeNotFoundException, InvalidNamespaceDefinitionException {

        // Initialise class attributes
        this.baseFilename = baseFilename;
        this.baseOutputFormat = baseOuputFormat;
        this.outputFolder = outputFolder;
        this.smicConfigurationFileName = smicConfigurationFileName;
        this.tempFolder = tempFolder;

        // Convert output from PDF to XML if base output format is specified as XML
        if (this.baseOutputFormat.equals("XML")) {
            this.baseFormatOutputConversion();
        } else {
            this.convertFormat();
        }
    }

    /**
     * Convert a given PDF file to XML format and switch base and secondary file formats name
     * 
     * @throws ConfigurationFileNotFoundException
     * @throws IOException
     * @throws InvalidConfigurationException
     * @throws FileNotFoundException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws MissiveXmlNotFoundException
     * @throws UnsupportedEncodingException
     * @throws InvalidDocumentObjectTemplateException
     * @throws TransformerConfigurationException
     * @throws TransformerException
     * @throws XmlDocumentNotGeneratedException
     * @throws XPathExpressionException
     * @throws ContainerNodeNotFoundException
     * @throws InvalidNamespaceDefinitionException
     */
    private void baseFormatOutputConversion() throws ConfigurationFileNotFoundException, IOException,
            InvalidConfigurationException, FileNotFoundException, ParserConfigurationException, SAXException,
            MissiveXmlNotFoundException, UnsupportedEncodingException, InvalidDocumentObjectTemplateException,
            TransformerConfigurationException, TransformerException, XmlDocumentNotGeneratedException,
            XPathExpressionException, ContainerNodeNotFoundException, InvalidNamespaceDefinitionException {

        // SMIC module instance
        Smic smic = new Smic(this.smicConfigurationFileName, this.outputFolder, this.tempFolder);

        // Convert the generated PDF document to XML
        String convertedFilename = smic.smicPdf2Xml(this.baseFilename);

        // Switch filenames
        String tmpSecondaryFilename = this.baseFilename;
        this.baseFilename = convertedFilename;
        this.secondaryFilename = tmpSecondaryFilename;

        // Move the secondary file to the temporary folder
        Utilities.moveFile(this.secondaryFilename, this.tempFolder);

        // Set the secondary filename
        this.secondaryFilename = this.tempFolder + System.getProperty("file.separator") +
                Utilities.getFilename(this.secondaryFilename);
    }

    /**
     * Convert the base format request for payment document to secondary format
     * 
     * @throws ConfigurationFileNotFoundException
     * @throws IOException
     * @throws InvalidConfigurationException
     * @throws FileNotFoundException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws MissiveXmlNotFoundException
     * @throws UnsupportedEncodingException
     * @throws InvalidDocumentObjectTemplateException
     * @throws TransformerConfigurationException
     * @throws TransformerException
     * @throws XmlDocumentNotGeneratedException
     * @throws XPathExpressionException
     * @throws ContainerNodeNotFoundException
     * @throws InvalidNamespaceDefinitionException
     */
    private void convertFormat() throws ConfigurationFileNotFoundException, IOException,
            InvalidConfigurationException, FileNotFoundException, ParserConfigurationException, SAXException,
            MissiveXmlNotFoundException, UnsupportedEncodingException, InvalidDocumentObjectTemplateException,
            TransformerConfigurationException, TransformerException, XmlDocumentNotGeneratedException,
            XPathExpressionException, ContainerNodeNotFoundException, InvalidNamespaceDefinitionException {

        // SMIC module instance
        Smic smic = new Smic(this.smicConfigurationFileName, this.tempFolder, this.tempFolder);

        // Convert PDF document to XML
        this.secondaryFilename = smic.smicPdf2Xml(this.baseFilename);
    }
}
