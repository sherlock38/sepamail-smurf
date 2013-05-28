package smurf.utilities;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import org.openconcerto.modules.finance.payment.ebics.EbicsConfiguration;
import org.openconcerto.modules.finance.payment.ebics.Host;
import org.openconcerto.modules.finance.payment.ebics.Partner;
import org.openconcerto.modules.finance.payment.ebics.User;
import org.openconcerto.modules.finance.payment.ebics.request.HIARequest;
import org.openconcerto.modules.finance.payment.ebics.request.HPBRequest;
import org.openconcerto.modules.finance.payment.ebics.request.INIRequest;
import org.openconcerto.modules.finance.payment.ebics.response.HPBResponse;
import smurf.dao.ConfigurationDao;
import smurf.exceptions.ConfigurationFormatException;
import smurf.exceptions.EbicsParameterNotDefinedException;
import smurf.model.Configuration;

/**
 * SEPAMailEbicsAdapter class is used to connect to an eBICS server and transfer request for payment documents to the
 * server.
 * 
 * @author Bishan Kumar Madhoo <bishan.madhoo@idsoft.mu>
 * @version 0.1
 */
public class SEPAMailEbicsAdapter {

    private ArrayList<Configuration> configurations;
    private EbicsConfiguration ebicsConfiguration;
    private KeyStore ebicsKeyStore;
    private String e001;
    private Host host;
    private Partner partner;
    private RSAPublicKey serverPublicAuthenticationKey;
    private RSAPublicKey serverPublicEncryptionKey;
    private User user;
    private String x001;

    public static String KEYSTORE_TYPE = "PKCS12";
    public static String KEYSTORE_PROVIDER = "BC";

    /**
     * SEPAMailEbicsAdapter class constructor
     * 
     * @throws IOException
     * @throws ConfigurationFormatException
     * @throws EbicsParameterNotDefinedException
     * @throws KeyStoreException
     * @throws NoSuchProviderException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws Exception
     */
    public SEPAMailEbicsAdapter() throws IOException, ConfigurationFormatException, EbicsParameterNotDefinedException,
            KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, CertificateException, IOException,
            FileNotFoundException, Exception {

        // Initialise class attributes
        this.configurations = ConfigurationDao.getConfigurationDao().getConfigurations();
        this.ebicsKeyStore = KeyStore.getInstance(SEPAMailEbicsAdapter.KEYSTORE_TYPE,
                SEPAMailEbicsAdapter.KEYSTORE_PROVIDER);

        // Host ID index within configuration settings
        int hostIndex = this.configurations.indexOf(new Configuration("ebics.host"));

        // Check if the host ID has been defined
        String hostId = "";
        if (hostIndex > -1) {

            // eBICS host ID
            hostId = this.configurations.get(hostIndex).getStringVal();

        } else {

            // Throw exception since parameter has not been defined
            throw new EbicsParameterNotDefinedException("ebics.host");
        }

        // eBICS host URL index within configuration settings
        int urlIndex = this.configurations.indexOf(new Configuration("ebics.url"));

        // Check if the eBICS server URL has been defined
        String url = "";
        if (urlIndex > -1) {

            // eBICS host URL
            url = this.configurations.get(urlIndex).getStringVal();

        } else {

            // Throw exception since parameter has not been defined
            throw new EbicsParameterNotDefinedException("ebics.url");
        }

        // Initialise eBICS host attribute
        this.host = new Host(hostId, url);

        // E001 server digest index within configuration settings
        int e001Index = this.configurations.indexOf(new Configuration("ebics.e001"));

        // Check if the eBICS server E001 digest has been defined
        if (e001Index > -1) {

            // Initialise the eBICS server E001 digest
            this.e001 = this.configurations.get(e001Index).getStringVal();

        } else {

            // Throw exception since parameter has not been defined
            throw new EbicsParameterNotDefinedException("ebics.e001");
        }        

        // X001 server digest index within configuration settings
        int x001Index = this.configurations.indexOf(new Configuration("ebics.x001"));

        // Check if the eBICS server X001 digest has been defined
        if (x001Index > -1) {

            // Initialise the eBICS server X001 digest
            this.x001 = this.configurations.get(x001Index).getStringVal();

        } else {

            // Throw exception since parameter has not been defined
            throw new EbicsParameterNotDefinedException("ebics.x001");
        }

        // Partner ID index within configuration settings
        int partnerIndex = this.configurations.indexOf(new Configuration("ebics.partner"));

        // Check if the eBICS Partner ID has been defined
        if (partnerIndex > -1) {

            // Initialise the eBICS partner ID
            this.partner = new Partner(this.configurations.get(partnerIndex).getStringVal());

        } else {

            // Throw exception since parameter has not been defined
            throw new EbicsParameterNotDefinedException("ebics.partner");
        }

        // User ID index within configuration settings
        int userIndex = this.configurations.indexOf(new Configuration("ebics.user"));

        // Check if the eBICS user ID has been defined
        if (userIndex > -1) {

            // Initialise the eBICS user ID
            this.user = new User(this.configurations.get(userIndex).getStringVal());

        } else {

            // Throw exception since parameter has not been defined
            throw new EbicsParameterNotDefinedException("ebics.user");
        }

        // Initialise the eBICS configuration class
        this.ebicsConfiguration = new EbicsConfiguration(this.host, this.partner, this.user);

        // eBICS keys file index within configuration settings
        int ebicsKeysFileIndex = this.configurations.indexOf(new Configuration("ebics.cert"));

        // Check if the eBICS keys file has been defined
        String ebicsKeysFile = "";
        if (ebicsKeysFileIndex > -1) {
            ebicsKeysFile = this.configurations.get(ebicsKeysFileIndex).getStringVal();
        } else {

            // Throw exception since parameter has not been defined
            throw new EbicsParameterNotDefinedException("ebics.cert");
        }

        // eBICS keys file index within configuration settings
        int ebicsKeysFilePassPhraseIndex = this.configurations.indexOf(new Configuration("ebics.passphrase"));

        // Check if the eBICS keys file pass phrase has been defined
        String ebicsKeysFilePassPhrase = "";
        if (ebicsKeysFilePassPhraseIndex > -1) {
            ebicsKeysFilePassPhrase = this.configurations.get(ebicsKeysFilePassPhraseIndex).getStringVal();
        } else {

            // Throw exception since parameter has not been defined
            throw new EbicsParameterNotDefinedException("ebics.passphrase");
        }

        // Try to load the key store using the given pass phrase
        try (InputStream in = new FileInputStream(ebicsKeysFile)) {

            // Load key store
            this.ebicsKeyStore.load(in, ebicsKeysFilePassPhrase.toCharArray());

        } catch (IOException ex) {

            // Throw raised IOException
            throw ex;
        }

        // Set the eBICS configuration key store
        this.ebicsConfiguration.setKeyStore(this.ebicsKeyStore, ebicsKeysFilePassPhrase);
    }

    /**
     * Start the eBICS session by sending the INI, HIA and HPB request and obtain the authentication and encryption
     * public keys of the eBICS server
     * 
     * @throws GeneralSecurityException
     * @throws IOException
     * @throws Exception 
     */
    public void startEbicsSession() throws GeneralSecurityException, IOException, Exception {

        // Create eBICS INI request
        INIRequest iniRequest = new INIRequest(this.ebicsConfiguration);

        // Send the INI request to the eBICS server
        String iniResponse = iniRequest.send();

        // Create eBICS HIA request
        HIARequest hiaRequest = new HIARequest(this.ebicsConfiguration);

        // Send HIA request to the eBICS server
        String hiaResponse = hiaRequest.send();

        // Create HPB request
        HPBRequest hpbRequest = new HPBRequest(this.ebicsConfiguration);

        // Send the HPB request to the eBICS server
        String hpbResponseString = hpbRequest.send();

        // Send the HPB request to the eBICS server
        HPBResponse hpbResponse = new HPBResponse(this.ebicsConfiguration, hpbResponseString);

        // Set the authentication public key of the eBICS server
        this.ebicsConfiguration.saveBankPublicAuthenticationKey(hpbResponse.getPublicAuthenticationKey());

        // Set the encryption public key of the eBICS server
        this.ebicsConfiguration.saveBankPublicEncryptionKey(hpbResponse.getPublicEncryptionKey());
    }
    
    /**
     * Send the given file via eBICS
     * 
     * @param filename Path and name of file that needs to be sent via eBICS
     */
    public void send(String filename) {

    }
}
