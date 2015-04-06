/**
 * Send a tap transaction to the server
 */

package co.tapdatapp.tapandroid.remotedata;

import android.os.AsyncTask;

import org.json.JSONException;

import co.tapdatapp.tapandroid.arm.WrongCurrencyException;
import co.tapdatapp.tapandroid.history.HistorySyncTask;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.localdata.Transaction;
import co.tapdatapp.tapandroid.service.TapTxn;

public class TapTxnTask
extends AsyncTask<TapTxnTask.TapTxnInitiator, Void, Void> {

    public interface TapTxnInitiator {
        /**
         * retrieve the transaction for this operation
         */
        TapTxn getTapTxn();

        /**
         * Called when a successful network operation has occurred
         */
        void onTapNetComplete(Transaction t);

        /**
         * Called when the tapped tag doesn't match the provided
         * currency.
         */
        void tappedWrongCurrency(WrongCurrencyException wce);

        /**
         * called when an error occurs
         */
        void onTapNetError(Throwable t);
    }

    private TapTxnInitiator callback;
    private Throwable error;
    private Transaction result;

    protected Void doInBackground(TapTxnInitiator... callbacks) {
        if (callbacks.length != 1) {
            throw new AssertionError("Must provide 1 TapTxnInitiator");
        }
        callback = callbacks[0];
        TapTxn tapTxn = callback.getTapTxn();
        try {
            tapTxn.TapAfool();
            // After posting the transaction, sync our local list of
            // transactions to ensure we have a local copy
            HistorySyncTask hst = new HistorySyncTask();
            hst.syncWithServer();
            result = new Transaction();
            result.moveToSlug(tapTxn.getSlug());
        }
        catch (WebServiceError wse) {
            // This is rather complicated because it might be the
            // result of the wrong currency being used
            WebResponse response = wse.getWebResponse();
            int currencyId;
            try {
                // Try to get the desired currency
                currencyId = response.getJSON().getInt("tag_currency");
            }
            catch (JSONException je) {
                // If this occurs, there's no currency ID in the
                // error JSON, so just return the WebServiceError
                error = wse;
                return null;
            }
            CurrencyDAO currency = new CurrencyDAO();
            try {
                currency.ensureLocalCurrencyDetails(currencyId);
            }
            catch (WebServiceError wse0) {
                // This shouldn't happen, but if it does, just pass
                // it through to the user
                error = wse0;
                return null;
            }
            // If everything succeeds to this point, throw a
            // WrongCurrencyException, which allows the UI code to
            // know what currency was requested.
            error = new WrongCurrencyException(wse.getWebResponse(), currencyId);
        }
        catch (Throwable t) {
            error = t;
        }
        return null;
    }

    protected void onPostExecute(Void x) {
        if (error == null) {
            callback.onTapNetComplete(result);
        }
        else {
            if (error instanceof WrongCurrencyException) {
                callback.tappedWrongCurrency((WrongCurrencyException)error);
            }
            else {
                callback.onTapNetError(error);
            }
        }
    }
}
