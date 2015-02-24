
package co.tapdatapp.taptestserver.entities;

public class RedeemVoucherResponse {
  public int balance;
  public int amount_redeemed;
  public CurrencyObject currency = new CurrencyObject();
  
  public class CurrencyObject {
    public String icon;
    public String symbol;
    public String name;
    public int id;
  }
}
