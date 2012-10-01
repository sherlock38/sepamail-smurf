package smurf.dao;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import smurf.Smurf;
import smurf.model.AvisClient;
import smurf.model.Configuration;
import smurf.exceptions.ConfigurationFormatException;
import smurf.exceptions.DatesNotSpecifiedException;
import smurf.exceptions.EndDateNotSpecifiedException;
import smurf.exceptions.StartDateNotSpecifiedException;

/**
 * The AvisClientDao class is used to query the database and fetch the request for payment records matching the given
 * search criteria
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class AvisClientDao {

    private Date endDate;
    private Date startDate;
    private SimpleDateFormat simpleDateFormat;
    private String jdbc;
    private String password;
    private String sql;
    private String username;

    /**
     * AvisClientDao constructor
     * 
     * @throws IOException
     * @throws ConfigurationFormatException
     */
    public AvisClientDao() throws IOException, ConfigurationFormatException {

        // Initialise class attributes
        this.endDate = null;
        this.jdbc = "";
        this.password = "";
        this.simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.sql = "";
        this.startDate = null;
        this.username = "";

        // Collection of Configurations memory objects
        ArrayList<Configuration> configurations = ConfigurationDao.getConfigurationDao().getConfigurations();

        // JDBC connection string
        int databaseJdbcIndex = configurations.indexOf(new Configuration("database.jdbc"));
        if (databaseJdbcIndex > -1) {
            this.jdbc = configurations.get(databaseJdbcIndex).getStringVal();
        }

        // SQL statement for retrieving list of requests for payment
        int databaseSqlIndex = configurations.indexOf(new Configuration("database.sql"));
        if (databaseSqlIndex > -1) {
            this.sql = configurations.get(databaseSqlIndex).getStringVal();
        }

        // Database access username
        int databaseUsernameIndex = configurations.indexOf(new Configuration("database.user"));
        if (databaseUsernameIndex > -1) {
            this.username = configurations.get(databaseUsernameIndex).getStringVal();
        }

        // Database access password
        int databasePasswordIndex = configurations.indexOf(new Configuration("database.password"));
        if (databasePasswordIndex > -1) {
            this.password = configurations.get(databasePasswordIndex).getStringVal();
        }
    }

    /**
     * Get the list of payments within the specified start and end dates
     * 
     * @return List of request for payments
     * @throws SQLException
     * @throws IOException
     * @throws ConfigurationFormatException
     * @throws DatesNotSpecifiedException
     * @throws StartDateNotSpecifiedException
     * @throws EndDateNotSpecifiedException
     */
    public ArrayList<AvisClient> getAvisClients() throws SQLException, IOException, ConfigurationFormatException,
            DatesNotSpecifiedException, StartDateNotSpecifiedException, EndDateNotSpecifiedException {

        // New list of request for payments
        ArrayList<AvisClient> avisClients = new ArrayList<AvisClient>();

        // Collection of Configurations memory objects
        ArrayList<Configuration> configurations = ConfigurationDao.getConfigurationDao().getConfigurations();

        // Get payment request start date
        int paymentStartDateIndex = configurations.indexOf(new Configuration("payment.start"));
        if (paymentStartDateIndex > -1) {
            this.startDate = configurations.get(paymentStartDateIndex).getDateVal();
        }

        // Get payment request end date
        int paymentEndDateIndex = configurations.indexOf(new Configuration("payment.end"));
        if (paymentEndDateIndex > -1) {
            this.endDate = configurations.get(paymentEndDateIndex).getDateVal();
        }

        // Check if both the start and end dates have been defined
        if ((this.startDate != null) && (this.endDate != null)) {

            // Connect to the MySQL database server
            Connection conn = DriverManager.getConnection(this.jdbc, this.username, this.password);

            // Statement for retrieving dialer campaigns from the DialXpert database
            Statement statement = conn.createStatement();

            // Prequare SQL string for retrieving the list of payment requests
            String smurfSql = this.sql.replaceAll("#SMURF#DateTimeRequestBegin#",
                    "'" + this.simpleDateFormat.format(this.startDate) + "'");
            smurfSql = smurfSql.replaceAll("#SMURF#DateTimeRequestEnd#",
                    "'" + this.simpleDateFormat.format(this.endDate) + "'");

            // Get the list of dialer campaigns from the database
            ResultSet resultSet = statement.executeQuery(smurfSql);

            // Scan through the result set and build the list of payment requests
            while (resultSet.next()) {

                // Create instance of request for payment object
                AvisClient avisClient =
                        new AvisClient(BigInteger.valueOf(resultSet.getLong(Smurf.REQUEST_FOR_PAYMENT_ID_COL_NAME)));

                // Get result metadata
                ResultSetMetaData rsmd = resultSet.getMetaData();
                
                // Build the property list of the request for payment object
                for (int i = 1; i < rsmd.getColumnCount() + 1; i++) {

                    // Check that we are not adding the ID column to the property list
                    if (!rsmd.getColumnName(i).equals(Smurf.REQUEST_FOR_PAYMENT_ID_COL_NAME)) {

                        // Get the value associated with the column
                        avisClient.setAttribute(rsmd.getColumnLabel(i), resultSet.getObject(rsmd.getColumnLabel(i)));
                    }
                }

                // Add the payment request object to the list of payment requests using the obtained details
                avisClients.add(avisClient);
            }

            // Close the result set if it is not null and still open
            if (resultSet != null) {
                resultSet.close();
            }

            // Clear the SQL statement if it is not null and still open
            if (statement != null) {
                statement.close();
            }

            // Close the connection
            if (!conn.isClosed()) {
                conn.close();
            }

        } else if ((this.startDate == null) && (this.endDate == null)) {

            // Start and end dates have not been specified
            throw new DatesNotSpecifiedException();

        } else if (this.startDate == null) {

            // The start date has not been specified
            throw new StartDateNotSpecifiedException();

        } else if (this.endDate == null) {

            // The end date has not been specified
            throw new EndDateNotSpecifiedException();
        }

        return avisClients;
    }
}
