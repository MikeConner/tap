/**
 * Fetch the list of currencies + balances in the background and
 * call back to the requester with the result. Also ensures that
 * currency details for each currency that the user has a balance
 * for are loaded in the local DB.
 */

package co.tapdatapp.tapandroid.currency;

import android.os.AsyncTask;

import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.user.Account;
import co.tapdatapp.tapandroid.user.BalancesExpiredException;

public class GetAllBalancesTask
extends AsyncTask<GetAllBalancesTask.Callback, Void, Void> {

    public interface Callback {
        void onBalancesLoaded(BalanceList list);
        void onBalanceLoadFailed(Throwable t);
    }

    private Callback callback;
    private BalanceList balanceList;
    private Throwable error;

    @Override
    protected Void
    doInBackground(Callback... balancesActivities) {
        if (balancesActivities.length != 1) {
            throw new AssertionError("Must provide 1 callback class");
        }
        callback = balancesActivities[0];
        Account account = new Account();

        CurrencyDAO userBalance = new CurrencyDAO();
        try {
            try {
                balanceList = account.getBalances();
                userBalance.ensureLocalCurrencyDetails(balanceList);
                return null;
            }
            catch (BalancesExpiredException bee) {
                // If this happens, just proceed to fetch from network
            }
            balanceList = userBalance.getAllBalances();
            account.setBalances(balanceList);
            userBalance.ensureLocalCurrencyDetails(balanceList);
        }
        catch (Throwable t){
            error = t;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void x) {
        if(error == null){
            callback.onBalancesLoaded(balanceList);
        }
        else{
            callback.onBalanceLoadFailed(error);
        }
    }
}
