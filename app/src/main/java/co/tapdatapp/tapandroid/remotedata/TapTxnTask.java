/**
 * Send a tap transaction to the server
 */

package co.tapdatapp.tapandroid.remotedata;

import android.os.AsyncTask;

import co.tapdatapp.tapandroid.history.HistorySyncTask;
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
         * Called when a failure of any type occurs
         *
         * @param t Throwable object containing failure details
         */
        void onTapNetFailure(Throwable t);
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
            //@TODO This should go to the slug, but the slug is not
            // returned by the webservice, so we move to the most
            // recent transaction. The cases where this will be wrong
            // are rare, but fixing it by having the webservice return
            // the slug is simple, and will guarantee correctness.
            result.moveToByOrder(0);
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
            callback.onTapNetFailure(error);
        }
    }
}
