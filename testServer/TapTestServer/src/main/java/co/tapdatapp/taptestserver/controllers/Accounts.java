/**
 * Keep track of all accounts
 */
package co.tapdatapp.taptestserver.controllers;

import co.tapdatapp.taptestserver.entities.BalanceResponse;
import co.tapdatapp.taptestserver.entities.CreateAccountResponse;
import co.tapdatapp.taptestserver.entities.GetBalancesResponse;
import co.tapdatapp.taptestserver.entities.ResponseResponse;
import java.util.HashMap;
import java.util.UUID;

public class Accounts {
  
  private final HashMap<String, CreateAccountResponse> accounts = new HashMap<>();
  
  private final Balances balances;
  
  public Accounts(Balances b) {
    balances = b;
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
  
}
