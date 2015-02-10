package org.gdg.lille;

import com.google.common.collect.Iterables;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Path("/github")
public class GithubHookResource {

    private Logger logger = Logger.getLogger(GithubHookResource.class.getSimpleName());

    @POST
    @Produces("text/plain")
    public void hook(@Context HttpHeaders headers, String body)
    {
        MultivaluedMap<String, String> headerParams = headers.getRequestHeaders();

        for (Map.Entry<String, List<String>> entry : headerParams.entrySet()) {
            logger.log(Level.INFO, "Header : " + entry.getKey());
            logger.log(Level.INFO, "Values : " + Iterables.toString(entry.getValue()));
        }

        logger.log(Level.INFO, "BODY : " + body);
    }
}