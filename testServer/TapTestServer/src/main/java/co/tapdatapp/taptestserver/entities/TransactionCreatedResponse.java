
package co.tapdatapp.taptestserver.entities;

public class TransactionCreatedResponse {
  public String slug;
  public int amount;
  public int dollar_amount;
  public int currency_id;
  public int final_balance;
  public String tapped_user_thumb = "";
  public String tapped_user_name = "";
  public TransactionPayloadObject payload;
}
