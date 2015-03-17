/**
 * Sync up the list of currencies owned by this user in the background
 */

package co.tapdatapp.tapandroid.tags;

import android.os.AsyncTask;

import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.UserFriendlyError;
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
            currencyFailure(wse);
        }
        return null;
    }

    /**
     * Gets called when there's an error getting the currencies
     * @param t has the error data
     */
    protected void currencyFailure(Throwable t){
        try{
            throw t;
        }
        catch(UserFriendlyError ufe){
            TapApplication.errorToUser(ufe);
        }
        catch(Throwable catchall){
            TapApplication.unknownFailure(t);
        }
    }
}
