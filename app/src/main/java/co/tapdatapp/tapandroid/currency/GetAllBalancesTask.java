/**
 * Fetch the list of currencies + balances in the background and
 * call back to the requester with the result. Also ensures that
 * currency details for each currency that the user has a balance
 * for are loaded in the local DB.
 */

package co.tapdatapp.tapandroid.currency;

import android.os.AsyncTask;

import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.localdata.UserBalance;
import co.tapdatapp.tapandroid.remotedata.WebServiceError;

public class GetAllBalancesTask
extends AsyncTask<BalancesActivity, Void, Void> {

    private BalancesActivity callback;
    private BalanceList balanceList;

    @Override
    protected Void
    doInBackground(BalancesActivity... balancesActivities) {
        if (balancesActivities.length != 1) {
            throw new AssertionError("Must provide 1 callback class");
        }
        callback = balancesActivities[0];
        UserBalance userBalance = new UserBalance();
        try {
            balanceList = userBalance.getAllBalances();
            userBalance.ensureLocalCurrencyDetails(balanceList);
        }
        catch (WebServiceError wse) {
            TapApplication.unknownFailure(wse);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void x) {
        callback.onBalancesLoaded(balanceList);
    }
}
