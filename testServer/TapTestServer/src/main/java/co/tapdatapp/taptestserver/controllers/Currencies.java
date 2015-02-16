/**
 * Dummy currencies ...
 */
package co.tapdatapp.taptestserver.controllers;

import co.tapdatapp.taptestserver.entities.CurrencyResponse;
import co.tapdatapp.taptestserver.entities.DenominationResponse;

public class Currencies {
  
  private final Currency[] currencies;
  
  public Currencies() {
    currencies = new Currency[2];
    // 0 = bitcoin
    currencies[0] = new Currency(0, "bitcoin", "S");
    currencies[1] = new Currency(1, "Kennywood Bucks", "K");
  }
  
  public CurrencyResponse get(int id) {
    CurrencyResponse rv = new CurrencyResponse();
    Currency c = currencies[id];
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
  
  public class Currency {
    public int id;
    public String name;
    public String symbol;
    public int max_amount;
    
    public Currency(int i, String n, String s) {
      id = i;
      name = n;
      symbol = s;
      max_amount = 500;
    }
    
  }
  
}
