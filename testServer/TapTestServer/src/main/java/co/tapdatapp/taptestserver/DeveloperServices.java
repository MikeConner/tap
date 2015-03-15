/**
 * Service endpoints purely for development
 */
package co.tapdatapp.taptestserver;

import co.tapdatapp.taptestserver.controllers.Data;
import co.tapdatapp.taptestserver.controllers.ImageBuilder;
import co.tapdatapp.taptestserver.dev.Monitor;
import java.io.IOException;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/1/developer")
public class DeveloperServices {
  
  private static final Data data;
  
  static {
    data = new Data();
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
    Monitor.trace("Request image " + name);
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
  
  @GET
  @Path("reset")
  public Response reset() {
    Monitor.trace("Resetting data to initial seed");
    ServiceEndpoint.reset();
    return Response.ok().build();
  }
  
  @PUT
  @Path("store")
  public Response store(byte[] body) {
    Monitor.trace("Saving data");
    return Response.ok(data.storeNew(body)).build();
  }
  
  @GET
  @Path("file/{id}")
  @Produces({ MediaType.APPLICATION_OCTET_STREAM })
  public Response getFile(@PathParam("id") String id) {
    Response r;
    try {
      byte[] rData = data.fetch(id);
      if (rData == null) {
        Monitor.trace("Not found: " + id);
        r = Response.status(404).build();
      }
      else {
        Monitor.trace("Returning " + rData.length + " bytes of file " + id);
        r = Response.ok(rData).build();
      }
    }
    catch (Exception e) {
      Monitor.trace(e);
      r = Response.serverError().build();
    }
    return r;
  }
  
}
