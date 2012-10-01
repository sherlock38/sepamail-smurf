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
        return new File(path).getParentFile().getPath() + System.getProperty("file.separator") + ".";

        // Absolute current working directory - added for debugging
        //return new File(path).getParentFile().getPath() + System.getProperty("file.separator") + "./..";
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

        // ZIP output stream
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(archiveFile));

        // List of files that have to be added to the archive
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

         out.close();
    }
}
