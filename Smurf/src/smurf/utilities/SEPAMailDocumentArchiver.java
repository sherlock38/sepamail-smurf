package smurf.utilities;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import smurf.dao.ConfigurationDao;
import smurf.exceptions.ConfigurationFormatException;
import smurf.model.AvisClient;
import smurf.model.Configuration;

/**
 * SEPAMailDocumentArchiver class is used create an archive of request for payment documents together with the sent
 * documents voucher.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class SEPAMailDocumentArchiver {

    private String archiveFolderName;
    private String archiveFilename;
    private SimpleDateFormat archiveFilenameDateFormat;
    private ArrayList<Configuration> configurations;
    private String tempFolderName;

    /**
     * SEPAMailDocumentArchiver class constructor
     * 
     * @throws IOException
     * @throws ConfigurationFormatException
     */
    public SEPAMailDocumentArchiver() throws IOException, ConfigurationFormatException {

        // Initialise class attributes
        this.archiveFilenameDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        this.configurations = ConfigurationDao.getConfigurationDao().getConfigurations();

        // Get archive folder name
        this.archiveFolderName = "archive";
        int archiveFolderNameIndex = this.configurations.indexOf(new Configuration("folder.archive"));
        if (archiveFolderNameIndex > -1) {
            this.archiveFolderName = this.configurations.get(archiveFolderNameIndex).getStringVal();
        }

        // Create the archive folder if it does not exist
        Utilities.createFolderIfNotExist(Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator") +
                this.archiveFolderName);

        // Get archive filename
        this.archiveFilename = "archive_des_demandes_de_règlement";
        int archiveFilenameIndex = this.configurations.indexOf(new Configuration("batch.name"));
        if (archiveFilenameIndex > -1) {
            this.archiveFilename = this.configurations.get(archiveFilenameIndex).getStringVal();
        }

        // Get temporary folder name
        this.tempFolderName = "temp";
        int temporaryFolderIndex = this.configurations.indexOf(new Configuration("folder.temp"));
        if (temporaryFolderIndex > -1) {
            this.tempFolderName = this.configurations.get(temporaryFolderIndex).getStringVal();
        }

        // Create the temporary folder if it does not exist
        Utilities.createFolderIfNotExist(Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator") +
                this.tempFolderName);
    }

    /**
     * Create a ZIP archive containing the documents and voucher for the given list of request for payment
     * 
     * @param avisClients List of request for payment whose documents need to be added to the archive
     * @param voucherFilename Path and name of the voucher file
     * @return Path and name of the archive created
     * @throws IOException
     */
    public String createArchive(ArrayList<AvisClient> avisClients, String voucherFilename) throws IOException {

        // Name of archive
        String docArchiveFilename = this.archiveFilename + "_" + this.archiveFilenameDateFormat.format(new Date());

        // Temporary archive folder path and name
        String tempArchiveFolder = Utilities.getCurrentWorkingDirectory() + System.getProperty("file.separator") +
                this.tempFolderName + System.getProperty("file.separator") + docArchiveFilename;

        // Create temporary folder which will contain the files that needs to be archived
        Utilities.createFolderIfNotExist(tempArchiveFolder);

        // Copy voucher to temporary archive folder
        FileUtils.copyFile(new File(voucherFilename), new File(tempArchiveFolder +
                System.getProperty("file.separator") + new File(voucherFilename).getName()), true);

        // Scan the list of requests for payment objects that needs to be processed
        for (AvisClient avisClient : avisClients) {

            // Copy request for payment documents to temporary archive folder
            FileUtils.copyFile(new File(avisClient.getSmurfOutput().getBaseFilename()), new File(tempArchiveFolder +
                    System.getProperty("file.separator") +
                    new File(avisClient.getSmurfOutput().getBaseFilename()).getName()), true);
        }

        // Archive file path and name
        String documentsArchiveFilename = Utilities.getCurrentWorkingDirectory() +
                System.getProperty("file.separator") + this.archiveFolderName + System.getProperty("file.separator") +
                docArchiveFilename + ".zip";

        // Create the archive
        Utilities.createFolderZipArchive(new File(tempArchiveFolder), new File(documentsArchiveFilename));

        // Delete the content of the temporary archive folder
        Utilities.deleteFolderFiles(tempArchiveFolder);

        // Delete the temporary archive folder
        new File(tempArchiveFolder).delete();

        return documentsArchiveFilename;
    }
}
