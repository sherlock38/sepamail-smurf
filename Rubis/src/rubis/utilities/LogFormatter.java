package rubis.utilities;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Log formatter extends the Formatter class and formats the output of the logger in Syslog format
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class LogFormatter extends Formatter {

    /**
     * Format the log record in Syslog format
     * 
     * @param lr Log record
     * @return Formatted log record string
     */
    @Override
    public String format(LogRecord lr) {

        // String builder to create the log line string
        StringBuilder strBuilder = new StringBuilder(1000);

        // Message priority
        strBuilder.append("<");
        strBuilder.append(this.mapSeverity(lr.getLevel()));
        strBuilder.append("> ");

        // Date and time at which the event occured
        strBuilder.append(this.calcDate(lr.getMillis()));

        // Hostname of host running the application
        strBuilder.append(" ");
        strBuilder.append(this.getHostname());
        strBuilder.append(" ");
        
        // Application name with PID
        strBuilder.append("smurf:[");
        strBuilder.append(this.getPid());
        strBuilder.append("] ");
        
        // Sub-system name - here we use the name of the class where the exception got raised
        strBuilder.append(lr.getSourceClassName());
        strBuilder.append(": ");
        
        // Log entry message
        strBuilder.append(formatMessage(lr).replaceAll(System.getProperty("line.separator"), " "));
        
        // Terminate log entry
        strBuilder.append("\n");
        
        return strBuilder.toString();
    }

    /**
     * Map the Java logging level to that of Syslog level
     * 
     * @param level Log message severity level
     * @return Severity level mapped to that of Syslog
     */
    private int mapSeverity(Level level) {

        int syslogSeverity = 0;

        // Log level severity
        switch (level.intValue()) {

            case 1000:
                syslogSeverity = 1;
                break;

            case 900:
                syslogSeverity = 4;
                break;

            case 800:
                syslogSeverity = 6;
                break;

            case 700:
                syslogSeverity = 7;
                break;
        }
        
        return syslogSeverity;
    }
    
    /**
     * Format the date and time displayed for a log entry
     * 
     * @param millisecs Event time in milliseconds since 1970
     * @return Formatted Date and time
     */
    private String calcDate(long millisecs) {

        // Date and time at which log is being recorded
        Date date = new Date(millisecs);
        
        // Date formatter
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.S");
        
        // Set time zone for date formatters
        dateFormat.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        timeFormat.setTimeZone(new SimpleTimeZone(SimpleTimeZone.UTC_TIME, "UTC"));
        
        // Format the date shown in the log output
        return dateFormat.format(date) + "T" + timeFormat.format(date) + "Z";
    }

    /**
     * Get the hostname of the host running the application
     * 
     * @return Hostname of the host running the application
     */
    private String getHostname() {
        
        String hostname;

        try {
            
            InetAddress addr = InetAddress.getLocalHost();

            // Get hostname
            hostname = addr.getHostName();

        } catch (UnknownHostException e) {

            // Default hostname
            hostname = "localhost";
        }
        
        return hostname;
    }

    /**
     * Get the process ID of the application
     * 
     * @return Application process ID
     */
    private String getPid() {

        // Process ID
        String sysPid = ManagementFactory.getRuntimeMXBean().getName();

        // Get the index of the '@' character within the process ID
        int atIndex = sysPid.indexOf("@");

        // Remove hostname from PID
        if (atIndex > -1) {
            return sysPid.substring(0, atIndex);
        }

        return sysPid;
    }
}
