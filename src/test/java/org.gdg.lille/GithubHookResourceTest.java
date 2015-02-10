package org.gdg.lille;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;


public class GithubHookResourceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(GithubHookResource.class);
    }

    @Test
    public void test() {
        Response hello = target("/github").request().post(Entity.entity(
                "Hello",
                MediaType.TEXT_PLAIN
        ));
        assertEquals(204, hello.getStatus());
    }
}
