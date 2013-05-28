package smurf.utilities;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import smurf.dao.ConfigurationDao;
import smurf.exceptions.*;
import smurf.model.Configuration;

/**
 * SEPAMailDocumentMailer class is used to email the request for payment document and generate the log document for the
 * sent document
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 1.0
 */
public class SEPAMailDocumentMailer {

    private ArrayList<Configuration> configurations;
    private int port;
    private String host;
    private String password;
    private String recipientAddress;
    private String recipientName;
    private String senderAddress;
    private String senderName;
    private String username;

    /**
     * SEPAMailDocumentMailer default constructor
     * 
     * @throws IOException
     * @throws ConfigurationFormatException
     * @throws MailParameterNotDefinedException
     */
    public SEPAMailDocumentMailer() throws IOException, MailParameterNotDefinedException, ConfigurationFormatException {

        // Initialise class attributes
        this.configurations = ConfigurationDao.getConfigurationDao().getConfigurations();

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
            this.senderName = this.configurations.get(nameIndex).getStringVal();
            
            // Check that a value has been defined for the mail sender name
            if (this.senderName.length() < 1) {
                throw new MailParameterNotDefinedException("mail.name");
            }
        } else {
            throw new MailParameterNotDefinedException("mail.name");
        }

        // Get mail sender address
        int senderIndex = this.configurations.indexOf(new Configuration("mail.sender"));
        if (senderIndex > -1) {

            // Get parameter value
            this.senderAddress = this.configurations.get(senderIndex).getStringVal();
            
            // Check that a value has been defined for the sender address
            if (this.senderAddress.length() < 1) {
                throw new MailParameterNotDefinedException("mail.sender");
            }
        } else {
            throw new MailParameterNotDefinedException("mail.sender");
        }

        // Get mail recipient address
        int recipientIndex = this.configurations.indexOf(new Configuration("mail.recipient"));
        if (recipientIndex > -1) {

            // Get parameter value
            this.recipientAddress = this.configurations.get(recipientIndex).getStringVal();
            
            // Check that a value has been defined for the recipient address
            if (this.recipientAddress.length() < 1) {
                throw new MailParameterNotDefinedException("mail.recipient");
            }
        } else {
            throw new MailParameterNotDefinedException("mail.recipient");
        }

        // Get mail recipient name
        int recipientNameIndex = this.configurations.indexOf(new Configuration("mail.recipientname"));
        if (recipientNameIndex > -1) {

            // Get parameter value
            this.recipientName = this.configurations.get(recipientNameIndex).getStringVal();
            
            // Check that a value has been defined for the recipient name
            if (this.recipientName.length() < 1) {
                throw new MailParameterNotDefinedException("mail.recipientname");
            }
        } else {
            throw new MailParameterNotDefinedException("mail.recipientname");
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
     * @param filename File that needs to be sent as mail attachment
     * @throws MessagingException
     */
    public void send(String filename) throws MessagingException {

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

        // Email sender address
        Address sender = new InternetAddress("\"" + this.senderName + "\"< " + this.senderAddress + " >");

        // Email recipient address
        Address recipient = new InternetAddress("\"" + this.recipientName + "\"< " + this.recipientAddress + " >");

        // Create mail message
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(sender);
        msg.setRecipient(Message.RecipientType.TO, recipient);
        msg.setSubject("Avis de paiement SEPAmail");

        // Create and fill the first message part
        MimeBodyPart mbp1 = new MimeBodyPart();
        mbp1.setText("Avis de paiement SEPAmail");

        // Create the second message part
        MimeBodyPart mbp2 = new MimeBodyPart();

        // Attach the file to the message
        FileDataSource fds = new FileDataSource(filename);
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
}
