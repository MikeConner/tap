/**
 * AsyncTask that creates a new account on the server in the background
 */

package co.tapdatapp.tapandroid.remotedata;

import android.os.AsyncTask;

import co.tapdatapp.tapandroid.AccountStartActivity;
import co.tapdatapp.tapandroid.user.Account;

public class NewAccountTask extends AsyncTask<AccountStartActivity, Void, Void> {

    private AccountStartActivity callback;

    @Override
    protected Void doInBackground(AccountStartActivity... callbacks) {
        if (callbacks.length != 1) {
            throw new AssertionError("Must provide 1 callback class");
        }
        callback = callbacks[0];
        try {
            new Account().createNew();
        }
        catch (Exception e) {
            // @TODO tie in to better UI handling for errors
            throw new AssertionError(e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void x) {
        callback.newAccountComplete();
    }
}
