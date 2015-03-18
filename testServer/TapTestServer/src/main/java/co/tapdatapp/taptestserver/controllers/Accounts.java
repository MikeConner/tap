/**
 * Keep track of all accounts
 */
package co.tapdatapp.taptestserver.controllers;

import co.tapdatapp.taptestserver.dev.Monitor;
import co.tapdatapp.taptestserver.entities.BalanceResponse;
import co.tapdatapp.taptestserver.entities.CreateAccountResponse;
import co.tapdatapp.taptestserver.entities.GetBalancesResponse;
import co.tapdatapp.taptestserver.entities.PayloadDataObject;
import co.tapdatapp.taptestserver.entities.PayloadObject;
import co.tapdatapp.taptestserver.entities.ResponseResponse;
import co.tapdatapp.taptestserver.entities.TagDataRequest;
import co.tapdatapp.taptestserver.entities.TagResponse;
import co.tapdatapp.taptestserver.entities.UpdateAccountRequest;
import co.tapdatapp.taptestserver.entities.UserDetailsResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Accounts {
  
  private final HashMap<String, CreateAccountResponse> accounts = new HashMap<>();
  private final ArrayList<NfcTag> tags = new ArrayList<>();
  private final ArrayList<PayloadObject> payloads = new ArrayList<>();
  
  private final Balances balances;
  
  public Accounts(Balances b) {
    balances = b;
  }
  
  public void reset() {
    accounts.clear();
    tags.clear();
    payloads.clear();
  }
  
  public CreateAccountResponse create(String phoneSecret) {
    CreateAccountResponse rv = new CreateAccountResponse();
    rv.response.auth_token = UUID.randomUUID().toString().replace("-", "");
    rv.response.nickname = "Nick Named";
    accounts.put(rv.response.auth_token, rv);
    balances.createStarterBalances(rv.response.auth_token);
    return rv;
  }

  public ResponseResponse getBalances(String authId) {
    GetBalancesResponse rv = new GetBalancesResponse();
    HashMap<Integer, Integer> b = balances.getBalance(authId);
    // there must always be bitcoin and at least one other balance
    int count = b.size() - 1;
    rv.balances = new BalanceResponse[count];
    int current = 0;
    for (Integer cid : b.keySet()) {
      if (cid == 0) {
        // Special for bitcoin
        rv.btc_balance = b.get(cid);
        rv.dollar_balance = 0;
        rv.exchange_rate = 0;
      }
      else {
        rv.balances[current] = new BalanceResponse(cid, b.get(cid));
        current++;
      }
    }
    return new ResponseResponse(rv);
  }
  
  public TagResponse newTag(String authId, TagDataRequest info) {
    NfcTag tag = new NfcTag();
    tag.authId = authId;
    tag.currencyId = info.tag.currency_id;
    tag.name = info.tag.name;
    tag.generateId();
    tag.payloads = new PayloadObject[info.payloads.length];
    int i = 0;
    for (PayloadDataObject payload : info.payloads) {
      PayloadObject ob = new PayloadObject();
      ob.content_type = payload.content_type;
      ob.content = payload.content;
      ob.threshold = payload.threshold;
      ob.uri = payload.uri;
      ob.payload_image = payload.payload_image;
      ob.payload_thumb = payload.payload_thumb;
      ob.description = payload.description;
      ob.setTagId(tag.getId());
      ob.generateSlug();
      payloads.add(ob);
      tag.payloads[i] = ob;
      i++;
    }
    tags.add(tag);
    TagResponse rv = new TagResponse(tag);
    return rv;
  }
  
  public TagResponse[] getUserTags(String authId) {
    ArrayList<TagResponse> rv = new ArrayList<>();
    for (NfcTag oneTag : tags) {
      if (oneTag.authId.equals(authId)) {
        rv.add(new TagResponse(oneTag));
      }
    }
    return rv.toArray(new TagResponse[rv.size()]);
  }
   
  public PayloadObject getPayload(String slug) {
    for (PayloadObject p : payloads) {
      if (p.slugEquals(slug)) {
        return p;
      }
    }
    PayloadObject p = new PayloadObject();
    p.generateSlug();
    p.content_type = "image";
    p.payload_image = ImageBuilder.getURL(300, 300, "Dynamic Yapa");
    p.payload_thumb = ImageBuilder.getURL(100, 100, "Dynamic Yapa");
    p.content = "Dynamic Yapa";
    p.uri = ImageBuilder.getURL(400, 400, slug);
    return p;
  }

  public UserDetailsResponse userDetails(String authId) {
    UserDetailsResponse rv = new UserDetailsResponse();
    CreateAccountResponse account = accounts.get(authId);
    if (account != null) {
      rv.nickname = account.response.nickname;
      rv.email = "test@example.com";
      rv.satoshi_balance = balances.getBalance(authId).get(0);
      rv.profile_image = ImageBuilder.getURL(200, 200, "profile image");
      rv.profile_thumb = ImageBuilder.getURL(50, 50, "profile thumb");
      rv.inbound_btc_qrcode.inbound_btc_qrcode.url = ImageBuilder.getURL(200, 200, "QR Code");
      rv.outbound_btc_address = null; // Replicates current Ruby behavior
      rv.inbound_btc_address = "ABC123abc456xyz123XYZ4561029384alhglash";
    }
    return rv;
  }

  public Object update(String authId, UpdateAccountRequest request) {
    CreateAccountResponse account = accounts.get(authId);
    if (account != null) {
      if (request.user.name != null && !request.user.name.isEmpty()) {
        account.response.nickname = request.user.name;
        Monitor.trace("Set nickname to " + request.user.name);
      }
      if (request.user.email != null && !request.user.email.isEmpty()) {
        account.response.email = request.user.email;
        Monitor.trace("Set email to " + request.user.email);
      }
      if (request.user.mobile_profile_thumb_url != null
          && !request.user.mobile_profile_thumb_url.isEmpty()
      ) {
        account.response.profile_thumb = request.user.mobile_profile_thumb_url;
        Monitor.trace("Set thumbnail to " + request.user.mobile_profile_thumb_url);
      }
      return null;
    }
    else {
      Monitor.trace("No account found to modify: " + authId);
      throw new AssertionError("No such account");
    }
  }
}
