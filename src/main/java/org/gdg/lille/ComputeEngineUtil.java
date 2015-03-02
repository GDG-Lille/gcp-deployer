package org.gdg.lille;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.compute.Compute;
import com.google.api.services.compute.ComputeScopes;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.Properties;

import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;

public class ComputeEngineUtil {

    public static final String CE_STATUS_TERMINATED = "TERMINATED";
    public static final String CE_STATUS_RUNNING = "RUNNING";
    public static final String CE_STATUS_STOPPING = "STOPPING";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final Properties properties = new Properties();
    private static ComputeEngineUtil INSTANCE;

    private final String zoneName;
    private final String projectId;

    /**
     * Private Constructor
     *
     * @param zoneName zoneName
     * @param projectId project id
     */
    private ComputeEngineUtil(String zoneName, String projectId) {
        this.zoneName = zoneName;
        this.projectId = projectId;
    }

    public static ComputeEngineUtil getInstance() throws IOException {
        if (INSTANCE == null) {
            properties.load(ComputeEngineUtil.class.getResourceAsStream("/config.properties"));
            INSTANCE = new ComputeEngineUtil(properties.getProperty("instance.zonename"), properties.getProperty("instance.projectid"));
        }
        return INSTANCE;
    }

    public Compute.Instances getCEInstances() throws GeneralSecurityException, IOException {
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

    public String getZoneName() {
        return zoneName;
    }

    public String getProjectId() {
        return projectId;
    }

    private Credential authorize() throws GeneralSecurityException, IOException {
        URL url = GithubHookResource.class.getResource("/" + properties.getProperty("instance.p12"));
        File f;
        try {
            f = new File(url.toURI());
        } catch (URISyntaxException e) {
            f = new File(url.getPath());
        }

        return new GoogleCredential.Builder().setTransport(
                newTrustedTransport())
                .setJsonFactory(JSON_FACTORY)
                .setServiceAccountId(properties.getProperty("instance.serviceaccountid"))
                .setServiceAccountScopes(ComputeScopes.all())
                .setServiceAccountPrivateKeyFromP12File(f)
                .build();
    }
}
