package org.gdg.lille.util;

import com.google.api.services.compute.model.Instance;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.SocketException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JenkinsUtil {

    private static final Properties properties = new Properties();
    private Logger logger = Logger.getLogger(JenkinsUtil.class.getSimpleName());
    private static JenkinsUtil INSTANCE;

    public static JenkinsUtil getInstance() throws IOException {
        if (INSTANCE == null) {
            properties.load(JenkinsUtil.class.getResourceAsStream("/config.properties"));
            INSTANCE = new JenkinsUtil();
        }
        return INSTANCE;
    }

    public boolean isJobsRunningOrInstanceStarting(Instance instance) throws IOException {
        String jenkinsUrl = "http://" + instance.getNetworkInterfaces().get(0).getAccessConfigs().get(0).getNatIP() + "/jenkins/api/json?depth=2";

        // Jenkins available ?
        final HttpGet httpGet = new HttpGet(jenkinsUrl);
        HttpResponse response = execute(httpGet, instance);
        if (response.getStatusLine().getStatusCode() != 200) {
            return true;
        }

        // Is any job running ?
        Gson gson = new GsonBuilder().create();
        Map content = gson.fromJson(new InputStreamReader(response.getEntity().getContent(), "UTF-8"), Map.class);
        List<Map> jobs = (List<Map>) content.get("jobs");
        for (Map job : jobs) {
            boolean jobInQueue = (boolean) job.get("inQueue");
            if (jobInQueue) {
                logger.log(Level.INFO, "One job in Queue : " + job.get("name"));
                return true;
            } else {
                Map lastBuild = (Map) job.get("lastBuild");
                if (lastBuild != null && (boolean) lastBuild.get("building")) {
                    logger.log(Level.INFO, "One job building : " + job.get("name"));
                    return true;
                }
            }
        }
        return false;
    }

    public void launchJob(String jobName, Instance instance) throws IOException, InterruptedException {
        // Waiting 10 seconds to permit the launch of Jenkins
        Thread.sleep(10000);

        String jenkinsUrl = "http://" + instance.getNetworkInterfaces().get(0).getAccessConfigs().get(0).getNatIP() + "/jenkins";
        logger.log(Level.INFO, "Jenkins URL : " + jenkinsUrl);

        final HttpGet httpGet = new HttpGet(jenkinsUrl + "/api/json?depth=1");
        try {
            HttpResponse response = execute(httpGet, instance);
            while (response.getStatusLine().getStatusCode() != 200) {
                Thread.sleep(1000);
                logger.log(Level.INFO, "Impossible to access to Jenkins, http response : " + response.getStatusLine().getStatusCode() + ". Retrying.");
                response = execute(httpGet, instance);
            }


            // Is the job exists ?
            Gson gson = new GsonBuilder().create();
            Map content = gson.fromJson(new InputStreamReader(response.getEntity().getContent(), "UTF-8"), Map.class);
            List<Map> jobs = (List<Map>) content.get("jobs");
            boolean jobExists = false;
            for (Map job : jobs) {
                if (jobName.equals(job.get("name"))) {
                    jobExists = true;
                    break;
                }
            }

            if (jobExists) {
                final HttpPost build = new HttpPost(jenkinsUrl + "/job/" + jobName + "/build");
                logger.log(Level.INFO, "Job URL : " + jenkinsUrl + "/job/" + jobName);
                HttpResponse buildResponse = execute(build, instance);
                if (buildResponse.getStatusLine().getStatusCode() != 201) {
                    logger.log(Level.SEVERE, "Impossible to launch the job, http response : " + buildResponse.getStatusLine().getStatusCode());
                } else {
                    logger.log(Level.INFO, "job launched!");
                }
            } else {
                logger.log(Level.WARNING, "The job " + jobName + " does not exist.");
            }
        } catch (SocketException exception) {
            logger.log(Level.INFO, "Jenkins not started ? : " + exception.getMessage());
            launchJob(jobName, instance);
        }
    }

    private HttpResponse execute(HttpRequest httpRequest, Instance instance) throws IOException {

        CredentialsProvider provider = new BasicCredentialsProvider();
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(properties.getProperty("jenkins.user"), properties.getProperty("jenkins.token"));
        provider.setCredentials(AuthScope.ANY, credentials);
        HttpClient httpClient = HttpClientBuilder
                .create()
                .setDefaultCredentialsProvider(provider)
                .build();

        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate BASIC scheme object and add it to the local auth cache
        BasicScheme basicAuth = new BasicScheme();
        HttpHost targetHost = new HttpHost(instance.getNetworkInterfaces().get(0).getAccessConfigs().get(0).getNatIP(), 80, "http");
        authCache.put(targetHost, basicAuth);
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(provider);
        context.setAuthCache(authCache);

        return httpClient.execute(targetHost, httpRequest, context);
    }
}
