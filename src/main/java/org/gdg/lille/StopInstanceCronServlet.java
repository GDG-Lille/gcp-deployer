package org.gdg.lille;

import com.google.api.services.compute.model.Instance;
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

public class StopInstanceCronServlet extends HttpServlet {
    private Logger logger = Logger.getLogger(GithubHookServlet.class.getSimpleName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ComputeEngineUtil computeEngineUtil = ComputeEngineUtil.getInstance();
            Instance instance = computeEngineUtil.getJenkinsInstance();
            JenkinsUtil jenkinsUtil = JenkinsUtil.getInstance();

            logger.log(Level.INFO, "Current Status :" + instance.getStatus());

            if (ComputeEngineUtil.CE_STATUS_RUNNING.equals(instance.getStatus()) && !jenkinsUtil.isJobsRunningOrInstanceStarting(instance)) {
                computeEngineUtil.stopJenkinsInstance();
            }

            resp.setStatus(204);
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
        }
    }
}