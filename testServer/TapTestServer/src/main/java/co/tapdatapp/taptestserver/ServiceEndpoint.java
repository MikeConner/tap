/**
 * Defines all of the webservice endpoints
 */
package co.tapdatapp.taptestserver;

import co.tapdatapp.taptestserver.controllers.Accounts;
import co.tapdatapp.taptestserver.controllers.Currencies;
import co.tapdatapp.taptestserver.controllers.Balances;
import co.tapdatapp.taptestserver.controllers.ISO8601Format;
import co.tapdatapp.taptestserver.controllers.Transactions;
import co.tapdatapp.taptestserver.entities.CreateAccountRequest;
import co.tapdatapp.taptestserver.dev.Monitor;
import co.tapdatapp.taptestserver.entities.NewTransactionRequest;
import co.tapdatapp.taptestserver.entities.RedeemVoucherResponse;
import co.tapdatapp.taptestserver.entities.ResponseResponse;
import co.tapdatapp.taptestserver.entities.TagDataRequest;
import co.tapdatapp.taptestserver.entities.TagResponse;
import co.tapdatapp.taptestserver.entities.TransactionCreatedResponse;
import co.tapdatapp.taptestserver.entities.TransactionResponse;
import co.tapdatapp.taptestserver.entities.UpdateAccountRequest;
import java.text.ParseException;
import java.util.Date;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONException;

@Path("/1")
public class ServiceEndpoint {
  
  private final static String AUTH_TOKEN = "auth_token";
  
  private static final Balances balances;
  private static final Accounts accounts;
  private static final Currencies currencies;
  private static final Transactions transactions;
  
  static {
    currencies = new Currencies();
    balances = new Balances(currencies);
    accounts = new Accounts(balances);
    transactions = new Transactions(balances, accounts);
  }
  
  public static void reset() {
    balances.reset();
    accounts.reset();
    currencies.reset();
    transactions.reset();
  }
  
  @GET
  @Path("ping")
  @Produces({ MediaType.APPLICATION_JSON })
  public Response ping() {
    Monitor.trace("PING response");
    return Response.ok(new PingResponse()).build();
  }
  
  public class PingResponse {
    public String response = "Pong";
  }  
  
  @POST
  @Path("/registrations.json")
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public Response createAccount(CreateAccountRequest car) {
    Monitor.trace("createAccount was called");
    return Response.ok(accounts.create(car.phone_secret_key)).build();
  }
  
  @GET
  @Path("/users/me.json")
  @Produces({ MediaType.APPLICATION_JSON })
  public Response getUserDetails(@QueryParam(AUTH_TOKEN) String authId) {
    Monitor.trace("User details for " + authId);
    Object details = accounts.userDetails(authId);
    return Response.ok(new ResponseResponse(details)).build();
  }
  
  @PUT
  @Path("/users/me.json")
  @Consumes({ MediaType.APPLICATION_JSON })
  public Response updateUserDetails(
    String request,
    @QueryParam(AUTH_TOKEN) String authId
  ) throws JSONException {
    Monitor.trace("update account info for " + authId);
    return Response.ok(accounts.update(authId, new UpdateAccountRequest(request))).build();
  }
  
  @GET
  @Path("/currencies/")
  public Response getOwnedCurrencies(@QueryParam(AUTH_TOKEN) String authId) {
    Monitor.trace("Looking for owned currencies");
    int[] values = currencies.getOwnedCurrencies(authId);
    Monitor.trace("Returned " + values.length + " owned currencies");
    return Response.ok(new ResponseResponse(values)).build();
  }
  
  @GET
  @Path("/currencies/{id}.json")
  public Response getCurrency(@PathParam("id") int id) {
    Monitor.trace("getCurrency on ID " + id);
    return Response.ok(new ResponseResponse(currencies.get(id))).build();
  }
  
  @GET
  @Path("/users/balance_inquiry")
  public Response getBalances(@QueryParam(AUTH_TOKEN) String authId) {
    if (authId == null || authId.isEmpty()) {
      return Response.status(401).build();
    }
    ResponseResponse response = accounts.getBalances(authId);
    Monitor.trace("balance inquiry on auth_id " + authId);
    return Response.ok(response).build();
  }
  
  private final static String TAGS_PATH = "nfc_tags.json";
  
  @GET
  @Path(TAGS_PATH)
  @Produces({ MediaType.APPLICATION_JSON })
  public Response getUserTags(@QueryParam(AUTH_TOKEN) String authId) {
    TagResponse[] response = accounts.getUserTags(authId);
    Monitor.trace("Got " + response.length + " tags for " + authId);
    return Response.ok(new ResponseResponse(response)).build();
  }

  @POST
  @Path(TAGS_PATH)
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public Response createNewTag(
    TagDataRequest request,
    @QueryParam(AUTH_TOKEN) String authId
  ) {
    TagResponse response = accounts.newTag(authId, request);
    Monitor.trace("Saving new tag with id " + response.id);
    return Response.ok(new ResponseResponse(response)).build();
  }
  
  @PUT
  @Path(TAGS_PATH)
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public Response updateTag(
    TagDataRequest request,
    @QueryParam(AUTH_TOKEN) String authId
  ) {
    TagResponse response = accounts.updateTag(authId, request);
    Monitor.trace("Updating tag with id " + response.id);
    return Response.ok(new ResponseResponse(response)).build();
  }
  
  @POST
  @Path("transactions.json")
  @Produces({ MediaType.APPLICATION_JSON })
  @Consumes({ MediaType.APPLICATION_JSON })
  public Response newTransaction(NewTransactionRequest request) {
    if ("wrong".equalsIgnoreCase(request.tag_id)) {
      Monitor.trace("Responding that currency is wrong");
      return Response.ok(new WrongCurrencyResponse()).status(500).build();
    }
    TransactionCreatedResponse response = transactions.create(request);
    Monitor.trace(request.auth_token + " transaction on " + request.tag_id);
    return Response.ok(new ResponseResponse(response)).build();
  }
  
  public class WrongCurrencyResponse {
    public String error_description = "Wrong currency";
    public String user_error = "Wrong currency";
    public int tag_currency = 2;
  }
  
  @GET
  @Path("transactions")
  @Produces({ MediaType.APPLICATION_JSON })
  @Consumes({ MediaType.APPLICATION_JSON })
  public Response transactionList(
    @QueryParam(AUTH_TOKEN) String authId,
    @QueryParam("after") String afterDate
  ) throws ParseException {
    Monitor.trace(authId + " requesting transactions after " + afterDate);
    ISO8601Format df = new ISO8601Format();
    Date d = df.parse(afterDate);
    TransactionResponse[] rv = transactions.getTransactionsAfter(authId, d);
    Monitor.trace(authId + " returning " + rv.length + " transactions");
    return Response.ok(new ResponseResponse(rv)).build();
  }
  
  @PUT
  @Path("users/{id}/redeem_voucher")
  @Produces({ MediaType.APPLICATION_JSON })
  public Response redeemVoucher(
    @QueryParam(AUTH_TOKEN) String authId,
    @PathParam("id") String voucherId
  ) {
    Monitor.trace(authId + " redeeming voucher " + voucherId);
    RedeemVoucherResponse response = balances.redeemVoucher(authId, voucherId);
    return Response.ok(new ResponseResponse(response)).build();
  }
}
