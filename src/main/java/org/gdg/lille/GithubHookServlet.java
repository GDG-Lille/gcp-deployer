package org.gdg.lille;

import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GithubHookServlet extends HttpServlet {

    private Logger logger = Logger.getLogger(GithubHookServlet.class.getSimpleName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        startTask(req);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        startTask(req);
    }

    private void startTask(HttpServletRequest req) {
        final String jobName = req.getParameter("job");
        logger.log(Level.INFO, "launching startjob task, with job name : " + jobName);
        if (jobName != null) {
            QueueFactory.getDefaultQueue().addAsync(TaskOptions.Builder.withUrl("/startjobtask").param("job", jobName));
        }
    }
}