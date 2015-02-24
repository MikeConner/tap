/**
 * Keep track of balances for accounts
 */
package co.tapdatapp.taptestserver.controllers;

import co.tapdatapp.taptestserver.dev.Monitor;
import co.tapdatapp.taptestserver.entities.CurrencyResponse;
import co.tapdatapp.taptestserver.entities.RedeemVoucherResponse;
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
  
  public void reset() {
    balances.clear();
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

  public RedeemVoucherResponse redeemVoucher(String authId, String voucherId) {
    HashMap<Integer, Integer> userBalance = balances.get(authId);
    if (userBalance == null) {
      throw new NullPointerException("No user found for " + authId);
    }
    RedeemVoucherResponse rv = new RedeemVoucherResponse();
    String[] parts = getFakeVoucherParts(voucherId);
    int currencyId = Integer.parseInt(parts[1]);
    int amount = Integer.parseInt(parts[3]);
    Integer balance = userBalance.get(currencyId);
    if (balance == null) {
      balance = 0;
    }
    balance += amount;
    userBalance.put(currencyId, balance);
    rv.amount_redeemed = amount;
    rv.balance = balance;
    rv.currency.id = currencyId;
    CurrencyResponse cr = currencies.get(currencyId);
    rv.currency.name = cr.name;
    rv.currency.icon = cr.icon;
    rv.currency.id = currencyId;
    rv.currency.symbol = cr.symbol;
    return rv;
  }
  
  private String[] getFakeVoucherParts(String voucher) {
    String[] rv = new String[4];
    char[] chars = voucher.toCharArray();
    if (chars[0] != 'C' && chars[0] != 'c') {
      throw new AssertionError("invalid voucher, must start with 'C'");
    }
    rv[0] = "C";
    int i = 1;
    rv[1] = "";
    while (chars[i] != 'A' && chars[i] != 'a') {
      rv[1] += chars[i];
      i++;
    }
    i++;
    rv[2] = "A";
    rv[3] = "";
    while (i < chars.length) {
      rv[3] += chars[i];
      i++;
    }
    return rv;
  }
  
}
