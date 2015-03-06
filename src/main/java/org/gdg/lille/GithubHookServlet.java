package org.gdg.lille;

import com.google.api.services.compute.model.Instance;
import com.google.appengine.api.ThreadManager;
import org.gdg.lille.util.ComputeEngineUtil;
import org.gdg.lille.util.JenkinsUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GithubHookServlet extends HttpServlet {

    private Logger logger = Logger.getLogger(GithubHookServlet.class.getSimpleName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        final String jobName = req.getParameter("job");
        if (jobName != null) {
            Thread thread = ThreadManager.createBackgroundThread(new Runnable() {
                public void run() {
                    try {
                        ComputeEngineUtil computeEngineUtil = ComputeEngineUtil.getInstance();
                        Instance instance = computeEngineUtil.getJenkinsInstance();

                        logger.log(Level.INFO, "Current Status :" + instance.getStatus());

                        if (ComputeEngineUtil.CE_STATUS_TERMINATED.equals(instance.getStatus())) {
                            computeEngineUtil.startJenkinsInstance();
                            while (!ComputeEngineUtil.CE_STATUS_RUNNING.equals(instance.getStatus())) {
                                instance = computeEngineUtil.getJenkinsInstance();
                                logger.log(Level.INFO, "Instance starting !");
                                Thread.sleep(1000);
                            }
                        }
                        logger.log(Level.INFO, "Instance started !");
                        JenkinsUtil.getInstance().launchJob(jobName, instance);
                    } catch (GeneralSecurityException | InterruptedException | IOException e) {
                        logger.log(Level.SEVERE, e.getLocalizedMessage());
                    }
                }
            });
            thread.start();
        }
    }
}