/**
 * Send a tap transaction to the server
 */

package co.tapdatapp.tapandroid.remotedata;

import android.os.AsyncTask;

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
        void onTapNetComplete();

        /**
         * Called when a failure of any type occurs
         *
         * @param t Throwable object containing failure details
         */
        void onTapNetFailure(Throwable t);
    }

    private TapTxnInitiator callback;
    private Throwable error;

    protected Void doInBackground(TapTxnInitiator... callbacks) {
        if (callbacks.length != 1) {
            throw new AssertionError("Must provide 1 TapTxnInitiator");
        }
        callback = callbacks[0];
        TapTxn tapTxn = callback.getTapTxn();
        try {
            tapTxn.TapAfool();
        }
        catch (Throwable t) {
            error = t;
        }
        return null;
    }

    protected void onPostExecute(Void x) {
        if (error == null) {
            callback.onTapNetComplete();
        }
        else {
            callback.onTapNetFailure(error);
        }
    }
}
