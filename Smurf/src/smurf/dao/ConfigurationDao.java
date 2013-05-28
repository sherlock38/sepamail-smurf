package smurf.dao;

import java.io.*;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import org.apache.commons.lang3.StringUtils;
import smurf.Smurf;
import smurf.exceptions.ConfigurationFormatException;
import smurf.exceptions.DateParseException;
import smurf.model.Configuration;
import smurf.model.GridConfiguration;
import smurf.utilities.Utilities;

/**
 * The Configuration class allows to read and persist the Smurf application configuration settings
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class ConfigurationDao {

    private static ConfigurationDao configurationDao;

    private ArrayList<Configuration> configurations;
    private ArrayList<GridConfiguration> gridConfigurations;

    /**
     * Get the list of configuration settings for the current application
     * 
     * @return List of configuration settings for the current application
     */
    public ArrayList<Configuration> getConfigurations() {
        return configurations;
    }

    /**
     * Get the list of grid configuration settings for the current application
     * 
     * @return List of grid configuration settings for the current application
     */
    public ArrayList<GridConfiguration> getGridConfigurations() {
        return gridConfigurations;
    }

    /**
     * Configuration default constructor
     */
    private ConfigurationDao() throws IOException {

        // Initialise the list of configuration settings
        configurations = new ArrayList<>();

        // Initialise the list of data grid configuration settings
        gridConfigurations = new ArrayList<>();

        // Load configuration settings
        readConfiguration();
    }

    /**
     * Get an instance of the ConfigurationDao class
     * 
     * @return ConfigurationDao class instance
     * @throws IOException
     */
    public static synchronized ConfigurationDao getConfigurationDao() throws IOException {

        // Check if an instance of the Configuration access class has already been declared
        if (configurationDao == null) {
            configurationDao = new ConfigurationDao();
        }

        // Instance of configuration access class
        return configurationDao;
    }

    /**
     * Override the clone method to prevent cloning of the class
     * 
     * @return void
     * @throws CloneNotSupportedException 
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Save the application configuration settings from memory to file
     * 
     * @throws IOException
     * @throws ConfigurationFormatException
     */
    public void saveConfiguration() throws IOException, ConfigurationFormatException {

        // Configuration settings properties
        Properties rubisProperties = new Properties();

        try {
            
            // Scan the list of configuration objects and build a map of configuration settings for the application
            for (int i = 0; i < configurations.size(); i++) {

                // Current configuration
                Configuration configuration = configurations.get(i);

                // Add general configuration settings to the properties object
                rubisProperties.put(configuration.getPropertyName() + ".advanced",
                        configuration.getIsAdvanced().toString());
                rubisProperties.put(configuration.getPropertyName() + ".display",
                        Boolean.valueOf(configuration.getDisplay()).toString());
                rubisProperties.put(configuration.getPropertyName() + ".order",
                        new Integer(configuration.getOrder()).toString());
                rubisProperties.put(configuration.getPropertyName() + ".title", configuration.getTitle());
                rubisProperties.put(configuration.getPropertyName() + ".type", configuration.getClassTypeName());

                // Add the configuration settings value to the properties object
                switch (configuration.getClassTypeName()) {

                    case "boolean":
                        rubisProperties.put(configuration.getPropertyName() + ".value",
                                configuration.getBoolVal().toString());
                        break;

                    case "Date":
                        rubisProperties.put(configuration.getPropertyName() + ".value",
                                configuration.getDateStringVal());
                        break;

                    case "double":
                        rubisProperties.put(configuration.getPropertyName() + ".value",
                                configuration.getDoubleVal().toString());
                        break;

                    case "float":
                        rubisProperties.put(configuration.getPropertyName() + ".value",
                                configuration.getFloatStringVal());
                        break;

                    case "int":
                        rubisProperties.put(configuration.getPropertyName() + ".value",
                                configuration.getIntVal().toString());
                        break;

                    case "String":
                        rubisProperties.put(configuration.getPropertyName() + ".value", configuration.getStringVal());
                        break;
                }
            }

            // Scan the list of data grid configuration settings and build a map of data grid configuration settings for
            // the application
            for (int i = 0; i < gridConfigurations.size(); i++) {

                // Current data grid configuration
                GridConfiguration gridConfiguration = gridConfigurations.get(i);

                // Property name
                String propertyName = gridConfiguration.getPropertyName();
        
                // Add data grid configuration settings to the properties object
                rubisProperties.put(propertyName + ".alias", gridConfiguration.getAlias());
                rubisProperties.put(propertyName + ".align", gridConfiguration.getAlignment());
                rubisProperties.put(propertyName + ".fixed", Boolean.valueOf(gridConfiguration.isFixed()).toString());
                rubisProperties.put(propertyName + ".title", gridConfiguration.getTitle());
                rubisProperties.put(propertyName + ".width", new Integer(gridConfiguration.getWidth()).toString());
            }

            // Arraylist of configuration file lines
            ArrayList<String> lines = this.readConfigurationLineByLine();

            // Iterate through the list of property keys and build a configuration settings map
            for (Iterator<String> it = rubisProperties.stringPropertyNames().iterator(); it.hasNext();) {

                // Get the current key
                String key = it.next();

                // Set the content of the required configuration line
                lines.set(this.getLineIndex(lines, key), key + " = "
                        + Utilities.convert(rubisProperties.getProperty(key)));
            }

            // Write configuration lines to file
            this.writeLineByLine(lines);
  
            // Refresh the configuration list
            readConfiguration();

        } catch (IOException | ConfigurationFormatException ex) {
            throw ex;
        }
    }
    
    /**
     * Read the application configuration file and build a list of configuration settings that is kept in memory
     * 
     * @throws IOException
     */
    private void readConfiguration() throws IOException {

        // Configuration settings map
        HashMap<String, HashMap<String, String>> confMap = new HashMap<>();

        // Datagrid configuration settings map
        HashMap<String, HashMap<String, String>> gridMap = new HashMap<>();

        // Instance of Properties parser
        Properties rubisProperties = new Properties();

        // Clear the array list of configurations
        configurations.clear();

        // Load the application properties file
        try {

            rubisProperties.load(new FileInputStream(Utilities.getCurrentWorkingDirectory() + 
                    System.getProperty("file.separator") + Smurf.CONFIG_FILE_NAME));

            // Iterate through the list of property keys and build a configuration settings map
            for (Iterator<String> it = rubisProperties.stringPropertyNames().iterator(); it.hasNext();) {

                String groupKeyName, propertyName;

                // Get the current key
                String key = it.next();

                // Split the key by its class separator
                String[] keyParts = StringUtils.split(key, ".");

                // Check the number of key name parts obtained
                if (keyParts.length > 1) {

                    List keyNameParts = new LinkedList();

                    // Build the configuration setting group name
                    for (int i = 0; i < keyParts.length; i++) {

                        keyNameParts.add(keyParts[i]);

                        // Check if we are at the second last element key name section
                        if (i == keyParts.length - 2) {
                            break;
                        }
                    }

                    // Group key name
                    groupKeyName = StringUtils.join(keyNameParts.toArray(), ".");
                    propertyName = keyParts[keyParts.length - 1];

                } else {
                    groupKeyName = key;
                    propertyName = key;
                }

                // Check that we are not parsing a datagrid configuration setting
                if (!groupKeyName.startsWith("datagrid")) {

                    // Check if we already have the group key name in the configuration settings map
                    if (confMap.containsKey(groupKeyName)) {

                        // Get the current map for the given group key name
                        HashMap<String, String> currentMap = confMap.get(groupKeyName);

                        // Add the new key value pair in the map for the given group key name
                        currentMap.put(propertyName, rubisProperties.getProperty(key));

                        // Store the map for the given group key name
                        confMap.put(groupKeyName, currentMap);

                    } else {

                        // Map for group key and value pairs
                        HashMap<String, String> keyValuePairsMap = new HashMap<>();
                        keyValuePairsMap.put(propertyName, rubisProperties.getProperty(key));

                        // Create a new map entry for the group key name
                        confMap.put(groupKeyName, keyValuePairsMap);
                    }

                } else {

                    // Check if we already have the group key name in the data grid configuration settings map
                    if (gridMap.containsKey(groupKeyName)) {

                        // Get the current map for the given group key name
                        HashMap<String, String> currentMap = gridMap.get(groupKeyName);

                        // Add the new key value pair in the map for the given group key name
                        currentMap.put(propertyName, rubisProperties.getProperty(key));

                        // Store the map for the given group key name
                        gridMap.put(groupKeyName, currentMap);

                    } else {

                        // Map for group key and value pairs
                        HashMap<String, String> keyValuePairsMap = new HashMap<>();
                        keyValuePairsMap.put(propertyName, rubisProperties.getProperty(key));

                        // Create a new map entry for the group key name
                        gridMap.put(groupKeyName, keyValuePairsMap);
                    }
                }
            }

            // Scan the list of configuration settings map and build the list of configuration objects
            for (Iterator<String> it = confMap.keySet().iterator(); it.hasNext();) {

                // Group key name
                String groupKeyName = it.next();

                // Key/value pairs for the given group key name
                HashMap<String, String> keyValuePairsMap = confMap.get(groupKeyName);

                // Values for the configuration object
                String title = keyValuePairsMap.containsKey("title") ? keyValuePairsMap.get("title") : "";
                String type = keyValuePairsMap.containsKey("type") ? keyValuePairsMap.get("type") : "String";
                boolean isAdvanced = keyValuePairsMap.containsKey("advanced") ?
                        Boolean.parseBoolean(keyValuePairsMap.get("advanced")) : false;
                int order = keyValuePairsMap.containsKey("order") ? Integer.parseInt(keyValuePairsMap.get("order")) : 0;
                boolean display = keyValuePairsMap.containsKey("display") ?
                        Boolean.parseBoolean(keyValuePairsMap.get("display")) : false;

                // Configuration object
                Configuration configuration = new Configuration(groupKeyName, title, type, isAdvanced, order, display);

                try {

                    // Set the value of the configuration object
                    switch (type) {

                        case "boolean":
                            configuration.setBoolVal(Boolean.parseBoolean(keyValuePairsMap.get("value").toString()));
                            break;

                        case "Date":
                            configuration.setDateVal(keyValuePairsMap.get("value").toString());
                            break;

                        case "double":
                            configuration.setDoubleVal(Double.parseDouble(keyValuePairsMap.get("value").toString()));
                            break;

                        case "float":
                            configuration.setFloatVal(keyValuePairsMap.get("value").toString());
                            break;

                        case "int":
                            configuration.setIntVal(Integer.parseInt(keyValuePairsMap.get("value").toString()));
                            break;

                        case "String":
                            configuration.setStringVal(keyValuePairsMap.get("value").toString());
                            break;
                    }

                } catch (ConfigurationFormatException | DateParseException | ParseException ex) {

                    // Log messages for exception raised
                    Smurf.logController.log(Level.WARNING, ConfigurationDao.class.getSimpleName(),
                            ex.getLocalizedMessage());

                }

                // Add the configuration to the list of configuration settings
                configurations.add(configuration);
            }

            // Sort the configuration list
            Collections.sort(configurations);

            // Clear any configuration objects that might be held by the list of data grid configuration objects
            gridConfigurations.clear();

            // Scan the list of configuration settings map and build the list of configuration objects
            for (Iterator<String> it = gridMap.keySet().iterator(); it.hasNext();) {

                // Group key name
                String groupKeyName = it.next();

                // Key/value pairs for the given group key name
                HashMap<String, String> keyValuePairsMap = gridMap.get(groupKeyName);

                // Values for data grid configuration object
                String alias = keyValuePairsMap.containsKey("alias") ? keyValuePairsMap.get("alias") : "undefined";
                String align = keyValuePairsMap.containsKey("align") ? keyValuePairsMap.get("align") : "left";
                boolean fixed = keyValuePairsMap.containsKey("fixed") ?
                        Boolean.parseBoolean(keyValuePairsMap.get("fixed")) : false;
                String title = keyValuePairsMap.containsKey("title") ? keyValuePairsMap.get("title") : "";
                int width = keyValuePairsMap.containsKey("width") ?
                        Integer.parseInt(keyValuePairsMap.get("width")) : 200;

                // Validate the alignment value
                if (!((align.toLowerCase().equals("center")) || (align.toLowerCase().equals("left"))
                        || (align.toLowerCase().equals("right")))) {
                    align = "left";
                }

                // Get the data grid configuration setting order
                int order = Integer.parseInt(String.valueOf(groupKeyName.charAt(groupKeyName.length() - 1)));

                // Add the data grid configuration to the list of data grid configurations
                gridConfigurations.add(new GridConfiguration(groupKeyName, title, alias, order, width, align, fixed));
            }

            // Sort the data grid configuration list
            Collections.sort(gridConfigurations);

        } catch (IOException ex) {
            throw ex;
        }
    }

    /**
     * Read the application configuration file line by line and build the array list of configuration file lines
     * 
     * @return Array list of configuration file lines
     * @throws FileNotFoundException
     * @throws IOException 
     */
    private ArrayList<String> readConfigurationLineByLine() throws FileNotFoundException, IOException {

        String line;

        // List of lines that make up the configuration file
        ArrayList<String> lines = new ArrayList<>();

        // File input stream
        FileInputStream fstream = new FileInputStream(Utilities.getCurrentWorkingDirectory() +
                System.getProperty("file.separator") + Smurf.CONFIG_FILE_NAME);

        // Get configuration file contents as an array of lines
        try (DataInputStream in = new DataInputStream(fstream)) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));

            // Read the configuration file line by line and add each line to the array list of configuration file lines
            while ((line = br.readLine()) != null) {
                lines.add(line);
            }
        }
        
        return lines;
    }

    /**
     * Get the index of the line having the specified property name
     * 
     * @param lines Array list of lines
     * @param propertyName Property name
     * @return Index of the line having the specified property name
     */
    private int getLineIndex(ArrayList<String> lines, String propertyName) {

        // Scan the list of lines
        for (int i = 0; i < lines.size(); i++) {

            // Check if the current line contains the required property name
            if (lines.get(i).startsWith(propertyName)) {
                return i;
            }
        }

        return 0;
    }

    /**
     * Write the content of configuration settings to file
     * 
     * @param lines Configuration settings contents
     * @throws IOException 
     */
    private void writeLineByLine(ArrayList<String> lines) throws IOException {

        // File wrtier stream to write content to file
        FileWriter fw = new FileWriter(Utilities.getCurrentWorkingDirectory() + 
                System.getProperty("file.separator") + Smurf.CONFIG_FILE_NAME);

        // Write configuration file content line by line
        try (BufferedWriter bw = new BufferedWriter(fw)) {

            // Scan the list of lines
            for (int i = 0; i < lines.size(); i++) {

                // Add line content
                bw.write(lines.get(i));

                // Add line separator
                bw.write(System.getProperty("line.separator"));
            }
        }
    }
}
