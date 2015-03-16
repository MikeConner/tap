/**
 * Sync up the list of currencies owned by this user in the background
 */

package co.tapdatapp.tapandroid.tags;

import android.os.AsyncTask;

import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.remotedata.WebServiceError;

public class GetMyCurrenciesTask extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... params) {
        CurrencyDAO currency = new CurrencyDAO();
        try {
            currency.updateAllOwnedCurrencies();
        }
        catch (WebServiceError wse) {
            TapApplication.unknownFailure(wse);
        }
        return null;
    }
}
