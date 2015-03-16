/**
 * Async task to get the current balance for the active currency
 */

package co.tapdatapp.tapandroid.remotedata;

import android.os.AsyncTask;

import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.user.Account;


public class GetCurrentBalanceAsStringTask
extends AsyncTask<GetCurrentBalanceAsStringTask.Callback, Void, String> {

    public interface Callback {
        void setBalance(String s);
    }

    private Callback callback;

    @Override
    protected String doInBackground(Callback... params) {
        if (params.length != 1) {
            throw new AssertionError("Must provide one callback to execute()");
        }
        callback = params[0];
        String value = "error";
        try {
            Account account = new Account();
            CurrencyDAO currency = new CurrencyDAO();
            value = currency.getBalanceAsString(account.getActiveCurrency());
        }
        catch (Throwable t) {
            // Any error leaves the value @ "error"
        }
        return value;
    }

    protected void onPostExecute(String value) {
        callback.setBalance(value);
    }
}
