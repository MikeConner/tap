/**
 * Keep track of balances for accounts
 */
package co.tapdatapp.taptestserver.controllers;

import dev.Monitor;
import java.util.HashMap;

public class Balances {
  
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
  
}
