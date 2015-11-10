package org.gdg.lille.util;

import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.http.*;
import com.google.api.services.compute.model.Instance;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
    private static JenkinsUtil INSTANCE;
    private Logger logger = Logger.getLogger(JenkinsUtil.class.getSimpleName());

    public static JenkinsUtil getInstance() throws IOException {
        if (INSTANCE == null) {
            properties.load(JenkinsUtil.class.getResourceAsStream("/config.properties"));
            INSTANCE = new JenkinsUtil();
        }
        return INSTANCE;
    }

    public boolean isJobsRunningOrInstanceStarting(Instance instance) throws IOException {
        String jenkinsUrl = "http://" + instance.getNetworkInterfaces().get(0).getAccessConfigs().get(0).getNatIP() + "/api/json?depth=2";

        // Jenkins available ?
        HttpResponse response = execute("GET", new GenericUrl(jenkinsUrl), null);
        if (response.getStatusCode() != 200) {
            return true;
        }

        // Is any job running ?
        Gson gson = new GsonBuilder().create();
        Map content = gson.fromJson(new InputStreamReader(response.getContent(), "UTF-8"), Map.class);
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

    public void launchJob(String jobName, Instance instance) throws InterruptedException {
        // Waiting 10 seconds to permit the launch of Jenkins
        Thread.sleep(10000);

        String jenkinsUrl = "http://" + instance.getNetworkInterfaces().get(0).getAccessConfigs().get(0).getNatIP();
        logger.log(Level.INFO, "Jenkins URL : " + jenkinsUrl);

        try {
            HttpResponse response = execute("GET", new GenericUrl(jenkinsUrl + "/api/json?depth=1"), null);
            while (response.getStatusCode() != 200) {
                Thread.sleep(1000);
                logger.log(Level.INFO, "Impossible to access to Jenkins, http response : " + response.getStatusCode() + ". Retrying.");
                response = execute("GET", new GenericUrl(jenkinsUrl + "/api/json?depth=1"), null);
            }


            // Is the job exists ?
            Gson gson = new GsonBuilder().create();
            Map content = gson.fromJson(new InputStreamReader(response.getContent(), "UTF-8"), Map.class);
            List<Map> jobs = (List<Map>) content.get("jobs");
            boolean jobExists = false;
            for (Map job : jobs) {
                if (jobName.equals(job.get("name"))) {
                    jobExists = true;
                    break;
                }
            }

            if (jobExists) {
                logger.log(Level.INFO, "Job URL : " + jenkinsUrl + "/job/" + jobName);
                HttpResponse buildResponse = execute("POST", new GenericUrl(jenkinsUrl + "/job/" + jobName + "/build"), null);
                if (buildResponse.getStatusCode() != 201) {
                    logger.log(Level.SEVERE, "Impossible to launch the job, http response : " + buildResponse.getStatusCode());
                } else {
                    logger.log(Level.INFO, "job launched!");
                }
            } else {
                logger.log(Level.WARNING, "The job " + jobName + " does not exist.");
            }
        } catch (IOException exception) {
            logger.log(Level.INFO, "Jenkins not started ? : " + exception.getMessage());
            launchJob(jobName, instance);
        }
    }

    private HttpResponse execute(String method, GenericUrl url, HttpContent content) throws IOException {

        logger.log(Level.INFO, "Executing this method : " + method);
        logger.log(Level.INFO, "to This url : " + url);

        HttpTransport httpTransport = UrlFetchTransport.getDefaultInstance();
        HttpRequestFactory requestFactory =
                httpTransport.createRequestFactory(new HttpRequestInitializer() {
                    @Override
                    public void initialize(com.google.api.client.http.HttpRequest httpRequest) throws IOException {
                        HttpHeaders headers = new HttpHeaders();
                        headers.setBasicAuthentication(properties.getProperty("jenkins.user"), properties.getProperty("jenkins.token"));
                        httpRequest.setHeaders(headers);
                        httpRequest.setThrowExceptionOnExecuteError(false);
                    }
                });
        return requestFactory.buildRequest(method, url, content).setThrowExceptionOnExecuteError(false).execute();
    }
}
