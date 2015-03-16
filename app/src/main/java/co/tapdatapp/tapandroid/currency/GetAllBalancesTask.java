/**
 * Fetch the list of currencies + balances in the background and
 * call back to the requester with the result. Also ensures that
 * currency details for each currency that the user has a balance
 * for are loaded in the local DB.
 */

package co.tapdatapp.tapandroid.currency;

import android.os.AsyncTask;

import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.UserFriendlyError;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.remotedata.WebServiceError;

public class GetAllBalancesTask
extends AsyncTask<GetAllBalancesTask.Callback, Void, Void> {

    public interface Callback {
        void onBalancesLoaded(BalanceList list);
        void onBalanceLoadFailure(Throwable t);
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
        CurrencyDAO userBalance = new CurrencyDAO();
        try {
            balanceList = userBalance.getAllBalances();
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
            callback.onBalanceLoadFailure(error);
        }
    }
}
