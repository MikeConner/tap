/**
 * AsyncTask that creates a new account on the server in the background
 */

package co.tapdatapp.tapandroid.remotedata;

import android.os.AsyncTask;

import co.tapdatapp.tapandroid.AccountStartActivity;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.user.Account;

public class NewAccountTask
extends AsyncTask<AccountStartActivity, Void, Throwable> {

    private AccountStartActivity callback;

    @Override
    protected Throwable doInBackground(AccountStartActivity... callbacks) {
        if (callbacks.length != 1) {
            throw new AssertionError("Must provide 1 callback class");
        }
        callback = callbacks[0];
        try {
            new Account().createNew();
        }
        catch (Throwable e) {
            return e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Throwable t) {
        if (t != null) {
            TapApplication.handleFailures(t);
        }
        else {
            callback.newAccountComplete();
        }
    }
}
