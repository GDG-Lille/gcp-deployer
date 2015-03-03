package org.gdg.lille;

import com.google.api.services.compute.Compute;
import com.google.api.services.compute.model.Instance;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StopInstanceCron extends HttpServlet {
    private Logger logger = Logger.getLogger(GithubHookResource.class.getSimpleName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            ComputeEngineUtil computeEngineUtil = ComputeEngineUtil.getInstance();
            Compute.Instances instances = computeEngineUtil.getCEInstances();

            Compute.Instances.Get getInstance = instances.get(computeEngineUtil.getProjectId(), computeEngineUtil.getZoneName(), "bitnami-jenkins");
            Instance instance = getInstance.execute();

            logger.log(Level.INFO, "Current Status :" + instance.getStatus());

            if (ComputeEngineUtil.CE_STATUS_RUNNING.equals(instance.getStatus())) {
                instances.stop(computeEngineUtil.getProjectId(), computeEngineUtil.getZoneName(), instance.getName()).execute();
            }

            resp.setStatus(204);
        } catch (GeneralSecurityException e) {
            logger.log(Level.SEVERE, e.getLocalizedMessage());
        }
    }
}