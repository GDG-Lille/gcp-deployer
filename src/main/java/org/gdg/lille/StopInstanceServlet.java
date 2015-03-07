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

public class StopInstanceServlet extends HttpServlet {

    private Logger logger = Logger.getLogger(StopInstanceServlet.class.getSimpleName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        logger.log(Level.INFO, "launching stop task");
        QueueFactory.getDefaultQueue().addAsync(TaskOptions.Builder.withUrl("/stoptask"));
    }
}