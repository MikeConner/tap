/**
 * Keep track of all accounts
 */
package co.tapdatapp.taptestserver.controllers;

import co.tapdatapp.taptestserver.entities.BalanceResponse;
import co.tapdatapp.taptestserver.entities.CreateAccountResponse;
import co.tapdatapp.taptestserver.entities.GetBalancesResponse;
import co.tapdatapp.taptestserver.entities.PayloadCreateRequest;
import co.tapdatapp.taptestserver.entities.PayloadObject;
import co.tapdatapp.taptestserver.entities.ResponseResponse;
import co.tapdatapp.taptestserver.entities.TagResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Accounts {
  
  private final HashMap<String, CreateAccountResponse> accounts = new HashMap<>();
  private final ArrayList<TagResponse> tags = new ArrayList<>();
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
  
  public TagResponse newTag(String authId) {
    TagResponse rv = new TagResponse(authId);
    tags.add(rv);
    return rv;
  }
  
  public TagResponse[] getUserTags(String authId) {
    ArrayList<TagResponse> rv = new ArrayList<>();
    for (TagResponse oneTag : tags) {
      if (oneTag.belongsTo(authId)) {
        rv.add(oneTag);
      }
    }
    return rv.toArray(new TagResponse[rv.size()]);
  }
  
  public PayloadObject newPayload(String auth, PayloadCreateRequest payload) {
    PayloadObject ob = new PayloadObject();
    ob.text = payload.payload.content;
    ob.threshold = payload.payload.threshold;
    ob.uri = "";
    ob.payload_image = payload.payload.mobile_payload_image_url;
    ob.payload_thumb = payload.payload.mobile_payload_thumb_url;
    ob.setTagId(payload.tag_id);
    ob.generateSlug();
    payloads.add(ob);
    return ob;
  }
  
  public PayloadObject getPayload(String slug) {
    for (PayloadObject p : payloads) {
      if (p.slugEquals(slug)) {
        return p;
      }
    }
    PayloadObject p = new PayloadObject();
    p.generateSlug();
    p.payload_image = ImageBuilder.getURL(300, 300, "Dynamic Yapa");
    p.payload_thumb = ImageBuilder.getURL(100, 100, "Dynamic Yapa");
    p.text = "Dynamic Yapa";
    p.uri = ImageBuilder.getURL(400, 400, slug);
    return p;
  }
}
