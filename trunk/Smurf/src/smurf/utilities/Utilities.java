package smurf.utilities;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.swing.JOptionPane;
import smurf.controller.MainWindowController;

/**
 * Utilities class groups static methods used throughout the application
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class Utilities {

    /**
     * Get the current working directory of the application
     *
     * @return The absolute path to the application working directory
     */
    public static String getCurrentWorkingDirectory() {

        // Current class directory
        URL location = Utilities.class.getProtectionDomain().getCodeSource().getLocation();
        String path;

        try {
            path = URLDecoder.decode(location.getPath(), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            path = location.getPath();
        }

        // Absolute current working directory
        //return new File(path).getParentFile().getPath() + System.getProperty("file.separator") + ".";

        // Absolute current working directory - added for debugging
        return new File(path).getParentFile().getPath() + System.getProperty("file.separator") + "./..";
    }

    /**
     * Exit application when configuration file cannot be read
     */
    public static void errorReadingConfigurationFile() {

        // Display error message
        MainWindowController.getMainWindowController().showDialogMessage(
                "Une erreur est survenue lors de la lecture du fichier\nde configuration de l'application. "
                + "Veuillez vérifier\nque le fichier est à l'emplacement requi avant de\nrelancer l'application.",
                JOptionPane.WARNING_MESSAGE);

        // Exit application
        System.exit(0);
    }

    /**
     * Check if a specified folder exists and create it if required
     * 
     * @param folderName Path and name of folder
     */
    public static void createFolderIfNotExist(String folderName) {

        // Check if the folder name has been specified
        if ((folderName != null) && (folderName.length() > 0)) {

            // File object for specified path and folder name
            File folder = new File(folderName);

            // Check if the file exist and is a directory
            if (!((folder.exists()) && (folder.isDirectory()))) {

                // Create the required folder
                folder.mkdir();
            }
        }
    }

    /**
     * Convert a string to unicode characters
     * 
     * @param str String that needs to be converted
     * @return Converted string
     */
    public static String convert(String str) {

        StringBuffer ostr = new StringBuffer();

        for(int i = 0; i < str.length(); i++) {

            char ch = str.charAt(i);
            
            if ((ch >= 0x0020) && (ch <= 0x007e)) {
                ostr.append(ch);
            } else {

                ostr.append("\\u");
                String hex = Integer.toHexString(str.charAt(i) & 0xFFFF);

                for(int j = 0; j < 4 - hex.length(); j++) {
                    ostr.append("0");
                }

                ostr.append(hex.toLowerCase());
            }
        }

        // Unicode formatted string
        return (new String(ostr));
    }

    /**
     * Create a ZIP archive of the specified folder at the specified file and location
     * 
     * @param folder Folder that needs to be archived
     * @param archive Archive filename and location
     * @throws FileNotFoundException
     */
    public static void createFolderZipArchive(File folder, File archive) throws FileNotFoundException, IOException {

        BufferedInputStream origin;
        byte data[] = new byte[2048];

        // Archive file output stream
        FileOutputStream archiveFile = new FileOutputStream(archive);

        // Add files to ZIP archive
        try (ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(archiveFile))) {

            // List of files to add to the archive
            String files[] = folder.list();

            // Add the files to the archive
            for (int i = 0; i < files.length; i++) {

                // Current file input stream
                FileInputStream fi = new FileInputStream(folder.getAbsolutePath() + System.getProperty("file.separator")
                        + files[i]);

                origin = new BufferedInputStream(fi, 2048);
                ZipEntry entry = new ZipEntry(files[i]);
                out.putNextEntry(entry);

                int count;

                while((count = origin.read(data, 0, 2048)) != -1) {
                   out.write(data, 0, count);
                }

                origin.close();

             }
        }
    }

    /**
     * Delete all the file in the specified folder
     * 
     * @param folderPath Folder from which files will be deleted
     */
    public static void deleteFolderFiles(String folderPath) {

        // Output folder
        File outputFolder = new File(folderPath);

        // Check if the output folder exists
        if (outputFolder.exists()) {

            // Get the list of files in the output folder
            File[] outputContents = outputFolder.listFiles();

            // Scan the list of files and delete the files
            for (int i = 0; i < outputContents.length; i++) {

                // Check if the current file is a file
                if (outputContents[i].isFile()) {

                    // Delete the file
                    outputContents[i].delete();
                }
            }
        }
    }

    /**
     * Get the name of the file from a full path and filename string
     * 
     * @param fullPathAndFilename Full path and filename string
     * @return Name of file
     */
    public static String getFilename(String fullPathAndFilename) {

        // Get the parts of the full path and filename string
        String[] parts = fullPathAndFilename.split(System.getProperty("file.separator"));

        // Check if parts where found
        if (parts.length > 0) {
            return parts[parts.length - 1];
        } else {
            return "";
        }
    }

    /**
     * Move a given file defined by its path and name to a target folder
     * 
     * @param filename Path and name of file that needs to be moved
     * @param destinationFolder Path of destination folder
     */
    public static void moveFile(String filename, String destinationFolder) {

        // File that needs to be moved
        File targetFile = new File(filename);

        // Destination folder
        File destination = new File(destinationFolder);

        // Check if the destination folder exists
        if (destination.exists()) {

            // Check if the destination is a folder
            if (destination.isDirectory()) {

                // Move the file to its destination folder
                targetFile.renameTo(new File(destination, targetFile.getName()));
            }
        }
    }
}
