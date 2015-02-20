/**
 * Response to a request for currency details
 * 
 * { "name" : "?", "icon" : "?", "amount_per_dollar" : ?, "symbol" : "?", "max_amount" : ?,
 *  "denominations" : {
 *    "amount" : ?,
 *    "image" : "?"
 *  }
 * }
 * 
 */
package co.tapdatapp.taptestserver.entities;

public class CurrencyResponse {
  public String name;
  public String icon;
  public int amount_per_dollar;
  public String symbol;
  public int max_amount;
  public DenominationResponse[] denominations;
  
}
