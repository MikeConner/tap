/**
 * Keep track of balances for accounts
 */
package co.tapdatapp.taptestserver.controllers;

import co.tapdatapp.taptestserver.dev.Monitor;
import java.util.HashMap;

public class Balances {
  
  /**
   * auth_token maps to currency_id => balance
   */
  private final HashMap<String, HashMap<Integer, Integer>> balances;
  
  private final Currencies currencies;
  
  public Balances(Currencies c) {
    currencies = c;
    balances = new HashMap<>();
  }
  
  public void createStarterBalances(String authId) {
    HashMap<Integer, Integer> b = new HashMap<>();
    // Bitcoin
    b.put(0, 0);
    // Kennywood bucks
    b.put(1, 500);
    Monitor.trace("Created default balances for " + authId);
    balances.put(authId, b);
    Monitor.trace("Now have " + balances.size() + " users with balances");
  }

  public HashMap<Integer, Integer> getBalance(String authId) {
    Monitor.trace("Getting balances for " + authId);
    Monitor.trace(balances.size() + " users with balances to choose from");
    return balances.get(authId);
  }

  void debit(String auth_token, int currency_id, int amount) {
    HashMap<Integer, Integer> balance = balances.get(auth_token);
    if (balance != null) {
      Integer b = balance.get(currency_id);
      if (b != null) {
        // No checks, the balance could fall into the negative
        b = b - amount;
        balance.put(currency_id, b);
        Monitor.trace("Debit " + amount + " from currency " + currency_id);
      }
      else {
        // Intentionally ignore, means the client can charge with currencies
        // that it has no balance on.
      }
    }
    else {
      throw new AssertionError("No such account " + auth_token);
    }
  }
  
}
