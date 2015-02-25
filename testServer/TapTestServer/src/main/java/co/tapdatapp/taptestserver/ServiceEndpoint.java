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
import co.tapdatapp.taptestserver.entities.PayloadCreateRequest;
import co.tapdatapp.taptestserver.entities.PayloadObject;
import co.tapdatapp.taptestserver.entities.RedeemVoucherResponse;
import co.tapdatapp.taptestserver.entities.ResponseResponse;
import co.tapdatapp.taptestserver.entities.TagResponse;
import co.tapdatapp.taptestserver.entities.TransactionCreatedResponse;
import co.tapdatapp.taptestserver.entities.TransactionResponse;
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
  
  @POST
  @Path("/registrations.json")
  @Consumes({ MediaType.APPLICATION_JSON })
  @Produces({ MediaType.APPLICATION_JSON })
  public Response createAccount(CreateAccountRequest car) {
    Monitor.trace("createAccount was called");
    return Response.ok(accounts.create(car.phone_secret_key)).build();
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
    Monitor.trace("balance inquiry on auth_id " + authId);
    if (authId == null || authId.isEmpty()) {
      return Response.status(401).build();
    }
    return Response.ok(accounts.getBalances(authId)).build();
  }
  
  /**
   * Return list of all tags owned by this user
   * 
   * @param authId authentication token
   * @return Array of tag objects
   */
  @GET
  @Path("nfc_tags.json")
  @Produces({ MediaType.APPLICATION_JSON })
  public Response getUserTags(@QueryParam(AUTH_TOKEN) String authId) {
    TagResponse[] response = accounts.getUserTags(authId);
    Monitor.trace("Got " + response.length + " tags for " + authId);
    return Response.ok(new ResponseResponse(response)).build();
  }
  
  /**
   * Create a new tag
   * 
   * @param authId
   * @return 
   */
  @POST
  @Path("nfc_tags.json")
  @Produces({ MediaType.APPLICATION_JSON })
  public Response createTag(@QueryParam(AUTH_TOKEN) String authId) {
    TagResponse tag = accounts.newTag(authId);
    Monitor.trace(authId + " created new tag " + tag.tag_id);
    return Response.ok(new ResponseResponse(tag)).build();
  }
  
  @POST
  @Path("payloads.json")
  @Produces({ MediaType.APPLICATION_JSON })
  public Response createPayload(
    PayloadCreateRequest payload,
    @QueryParam(AUTH_TOKEN) String authId
  ) {
    PayloadObject createdPayload = accounts.newPayload(authId, payload);
    Monitor.trace(authId + " created payload " + createdPayload.getSlug());
    return Response.ok(new ResponseResponse(createdPayload.getSlug())).build();
  }
  
  @POST
  @Path("transactions.json")
  @Produces({ MediaType.APPLICATION_JSON })
  @Consumes({ MediaType.APPLICATION_JSON })
  public Response newTransaction(NewTransactionRequest request) {
    TransactionCreatedResponse response = transactions.create(request);
    Monitor.trace(request.auth_token + " transaction on " + request.tag_id);
    return Response.ok(new ResponseResponse(response)).build();
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
