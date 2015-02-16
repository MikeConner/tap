/**
 * Service endpoints purely for development
 */
package co.tapdatapp.taptestserver;

import co.tapdatapp.taptestserver.controllers.ImageBuilder;
import co.tapdatapp.taptestserver.dev.Monitor;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/1/developer")
public class DeveloperServices {
  
  @GET
  @Path("/ping")
  public Response ping() {
    Monitor.trace("ping was called");
    return Response.ok().build();
  }
  
  @GET
  @Path("/messages")
  @Produces({ MediaType.APPLICATION_JSON })
  public Response getMessages() {
    return Response.ok(Monitor.getMessages()).build();
  }
  
  @GET
  @Path("image/{name}")
  @Produces({ "image/png" })
  public Response getImage(@PathParam("name") String name) {
    Response r;
    try {
      r = Response.ok(ImageBuilder.getImage(name)).build();
    }
    catch (IOException ioe) {
      Monitor.trace(ioe);
      r = Response.serverError().build();
    }
    return r;
  }
  
}
