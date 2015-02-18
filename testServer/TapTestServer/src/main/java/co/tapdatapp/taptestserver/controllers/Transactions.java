
package co.tapdatapp.taptestserver.controllers;

import co.tapdatapp.taptestserver.entities.NewTransactionRequest;
import co.tapdatapp.taptestserver.entities.PayloadObject;
import co.tapdatapp.taptestserver.entities.TransactionCreatedResponse;
import co.tapdatapp.taptestserver.entities.TransactionPayloadObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Transactions {
  
  private final Balances balances;
  private final Accounts accounts;
  private final HashMap<String, ArrayList<Transaction>> transactions;
  
  public Transactions(Balances b, Accounts a) {
    balances = b;
    accounts = a;
    transactions = new HashMap<>();
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
    rv.payload = new TransactionPayloadObject(t.payload);
    rv.amount = t.amount;
    rv.currency_id = t.currency_id;
    return rv;
  }
  
  private class Transaction {
    public Date timestamp;
    public int amount;
    public int currency_id;
    public PayloadObject payload;
  }
  
}
