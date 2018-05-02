package net.michir.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * Created by michir on 30/04/2018.
 */
@Path("/private")
public class PrivateEndpoint {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ViewObject get() {
        return ViewObject.ofApp();
    }

    @Path("/mixed")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ViewObject getMixed() {
        return ViewObject.of();
    }

}
