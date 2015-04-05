/**
 * Sync up the list of currencies owned by this user in the background
 */

package co.tapdatapp.tapandroid.tags;

import android.os.AsyncTask;
import android.util.Log;

import co.tapdatapp.tapandroid.localdata.CurrencyDAO;

public class GetMyCurrenciesTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
        CurrencyDAO currency = new CurrencyDAO();
        try {
            currency.updateAllOwnedCurrencies();
        }
        catch (Throwable t) {
            // This task doesn't directly impact anything if it fails,
            // so just log it so we know
            Log.e("OWNED_CURRENCY_FETCH", "Failed", t);
        }
        return null;
    }
}
