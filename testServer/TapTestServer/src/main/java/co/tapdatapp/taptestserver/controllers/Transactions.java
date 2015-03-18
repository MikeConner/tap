
package co.tapdatapp.taptestserver.controllers;

import co.tapdatapp.taptestserver.entities.NewTransactionRequest;
import co.tapdatapp.taptestserver.entities.PayloadObject;
import co.tapdatapp.taptestserver.entities.TransactionCreatedResponse;
import co.tapdatapp.taptestserver.entities.TransactionPayloadObject;
import co.tapdatapp.taptestserver.entities.TransactionResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class Transactions {
  
  private final Balances balances;
  private final Accounts accounts;
  private final HashMap<String, ArrayList<Transaction>> transactions;
  
  public Transactions(Balances b, Accounts a) {
    balances = b;
    accounts = a;
    transactions = new HashMap<>();
  }
  
  /**
   * Deletes all data
   */
  public void reset() {
    transactions.clear();
  }
  
  public TransactionCreatedResponse create(NewTransactionRequest request) {
    balances.debit(request.auth_token, request.currency_id, request.amount);
    TransactionCreatedResponse rv = new TransactionCreatedResponse();
    Transaction t = new Transaction();
    t.amount = request.amount;
    t.timestamp = new Date();
    t.currency_id = request.currency_id;
    t.payload = accounts.getPayload(request.tag_id);
    ArrayList<Transaction> txns = transactions.get(request.auth_token);
    if (txns == null) {
      txns = new ArrayList<>();
      transactions.put(request.auth_token, txns);
    }
    txns.add(t);
    rv.slug = t.payload.getSlug();
    rv.payload = new TransactionPayloadObject(t.payload);
    rv.amount = t.amount;
    rv.currency_id = t.currency_id;
    return rv;
  }
  
  public TransactionResponse[]
  getTransactionsAfter(String authId, Date afterDate) {
    ArrayList<TransactionResponse> rv = new ArrayList<>();
    ArrayList<Transaction> input = transactions.get(authId);
    if (input != null) {
      ISO8601Format df = new ISO8601Format();
      for (Transaction oneT : input) {
        if (oneT.timestamp.after(afterDate)) {
          TransactionResponse t = new TransactionResponse();
          t.id = oneT.payload.getSlug();
          t.date = df.format(oneT.timestamp);
          t.payload_image = oneT.payload.payload_image;
          t.payload_thumb = oneT.payload.payload_thumb;
          t.payload_content_type = "image";
          t.amount = oneT.amount;
          t.dollar_amount = 0;
          t.comment = oneT.payload.description;
          t.other_user_thumb = ImageBuilder.getURL(100, 100, "otherUserThumb");
          t.other_user_nickname = "Other User Nickname";
          rv.add(t);
        }
      }
    }
    return rv.toArray(new TransactionResponse[rv.size()]);
  }
  
  private class Transaction {
    public Date timestamp;
    public int amount;
    public int currency_id;
    public PayloadObject payload;
  }
  
}
