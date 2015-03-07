package org.gdg.lille.util;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;
import com.google.api.services.compute.model.Instance;
import org.gdg.lille.tasks.StartJobTask;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;

public class ComputeEngineUtil {

    public static final String CE_STATUS_TERMINATED = "TERMINATED";
    public static final String CE_STATUS_RUNNING = "RUNNING";
    public static final String CE_STATUS_STOPPING = "STOPPING";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Properties properties = new Properties();
    private static ComputeEngineUtil INSTANCE;

    private Logger logger = Logger.getLogger(JenkinsUtil.class.getSimpleName());

    private final String zoneName;
    private final String projectId;
    private final String jenkinsName;

    /**
     * Private Constructor
     *
     * @param zoneName    zoneName
     * @param projectId   project id
     * @param jenkinsName jenkins instance name
     */
    private ComputeEngineUtil(String zoneName, String projectId, String jenkinsName) {
        this.zoneName = zoneName;
        this.projectId = projectId;
        this.jenkinsName = jenkinsName;
    }

    public static ComputeEngineUtil getInstance() throws IOException {
        if (INSTANCE == null) {
            properties.load(ComputeEngineUtil.class.getResourceAsStream("/config.properties"));
            INSTANCE = new ComputeEngineUtil(
                    properties.getProperty("instance.zonename"),
                    properties.getProperty("instance.projectid"),
                    properties.getProperty("instance.jenkinsname"));
        }
        return INSTANCE;
    }

    public Instance getJenkinsInstance() throws GeneralSecurityException, IOException {
        return this.getCEInstances().get(this.projectId, this.zoneName, this.jenkinsName).execute();
    }

    public void startJenkinsInstance() throws GeneralSecurityException, IOException {
        this.getCEInstances().start(this.projectId, this.zoneName, this.jenkinsName).execute();
        logger.log(Level.INFO, this.jenkinsName + " instance start command launched.");
    }

    public void stopJenkinsInstance() throws GeneralSecurityException, IOException {
        this.getCEInstances().stop(this.projectId, this.zoneName, this.jenkinsName).execute();
        logger.log(Level.INFO, this.jenkinsName + " instance stop command launched.");
    }

    private Credential authorize() throws GeneralSecurityException, IOException {
        URL url = StartJobTask.class.getResource("/" + properties.getProperty("instance.p12"));
        File f;
        try {
            f = new File(url.toURI());
        } catch (URISyntaxException e) {
            f = new File(url.getPath());
        }

        return new GoogleCredential.Builder()
                .setTransport(newTrustedTransport())
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(properties.getProperty("instance.serviceaccountid"))
                .setServiceAccountScopes(ComputeScopes.all())
                .setServiceAccountPrivateKeyFromP12File(f)
                .build();
    }

    private Compute.Instances getCEInstances() throws GeneralSecurityException, IOException {
        HttpTransport httpTransport = newTrustedTransport();
        // authorize
        Credential credential = authorize();
        // Create compute engine object for listing instances
        Compute compute = new Compute.Builder(httpTransport, JSON_FACTORY, null)
                .setApplicationName(projectId)
                .setHttpRequestInitializer(credential)
                .build();

        return compute.instances();
    }
}
