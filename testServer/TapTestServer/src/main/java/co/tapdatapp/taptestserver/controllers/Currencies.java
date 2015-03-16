/**
 * Dummy currencies ...
 */
package co.tapdatapp.taptestserver.controllers;

import co.tapdatapp.taptestserver.entities.CurrencyResponse;
import co.tapdatapp.taptestserver.entities.DenominationResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class Currencies {
  
  private HashMap<Integer, Currency> currencies;
  
  public Currencies() {
    reset();
  }
  
  public final void reset() {
    currencies = new HashMap<>();
    // 0 = bitcoin
    currencies.put(0, new Currency(0, "bitcoin", "S", null));
    currencies.put(1, new Currency(1, "Kennywood Bucks", "K", null));
    currencies.put(2, new Currency(2, "Super bucks", "$", null));
  }
  
  public CurrencyResponse get(int id) {
    CurrencyResponse rv = new CurrencyResponse();
    Currency c = currencies.get(id);
    rv.amount_per_dollar = 0;
    rv.icon = ImageBuilder.getURL(200, 200, c.name);
    rv.max_amount = c.max_amount;
    rv.name = c.name;
    rv.symbol = c.symbol;
    rv.denominations = new DenominationResponse[3];
    rv.denominations[0] = new DenominationResponse(1, ImageBuilder.getURL(275, 100, "1"));
    rv.denominations[1] = new DenominationResponse(5, ImageBuilder.getURL(275, 100, "5"));
    rv.denominations[2] = new DenominationResponse(10, ImageBuilder.getURL(275, 100, "10"));
    return rv;
  }
  
  public void createUserCurrency(String authId) {
    int id = 0;
    Set<Integer> keys = currencies.keySet();
    for (Integer key : keys) {
      if (key > id) {
        id = key;
      }
    }
    id++;
    Currency c = new Currency(id, authId, "u", authId);
    currencies.put(id, c);
  }
  
  public int[] getOwnedCurrencies(String authId) {
    ArrayList<Integer> values = new ArrayList<>();
    for (int currencyId : currencies.keySet()) {
      Currency c = currencies.get(currencyId);
      if (authId.equals(c.owner)) {
        values.add(currencyId);
      }
    }
    int[] rv = new int[values.size()];
    for (int i = 0; i < rv.length; i++) {
      rv[i] = values.get(i);
    }
    return rv;
  }
  
  public class Currency {
    public int id;
    public String name;
    public String symbol;
    public String owner;
    public int max_amount;
    
    public Currency(int i, String n, String s, String o) {
      id = i;
      name = n;
      symbol = s;
      owner = o;
      max_amount = 500;
    }
    
  }
  
}
