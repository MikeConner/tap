/**
 * This activity is launched if the app does not have an account
 * configured. At this time, it simply presents a busy spinner and
 * creates a random account on the server.
 *
 * @TODO allow this to connect to an existing account
 */

package co.tapdatapp.tapandroid;

import android.app.Activity;
import android.os.Bundle;

import co.tapdatapp.tapandroid.remotedata.NewAccountTask;
import co.tapdatapp.tapandroid.remotedata.NoNetworkError;

public class AccountStartActivity extends Activity {

    public final static int ACCOUNT_CREATION = 1;

    /**
     * Just display an indefinite progress meter. In the near future,
     * this layout will give the user the opportunity to either
     * connect to an existing account or create a new one.
     *
     * @param state Per spec
     */
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_account_start);
    }

    /**
     * Long-term, this will give the option to create a new account
     * or log in to an old account. Short term, always create a new
     * account.
     */
    @Override
    public void onResume() {
        super.onResume();
        new NewAccountTask().execute(this);
    }

    /**
     * When successful, exit this Activity, which will return to the
     * MainActivity with a created account.
     */
    public void newAccountComplete() {
        setResult(RESULT_OK);
        finish();
    }

    /**
     * On failure, display a message and exit
     *
     * @param t The cause of the failure
     */
    // @TODO probably some better error handling
    public void newAccountError(Throwable t) {
        if (t instanceof NoNetworkError) {
            TapApplication.errorToUser(TapApplication.string(R.string.no_network));
        }
        else {
            TapApplication.handleFailures(this, t);
        }
        setResult(RESULT_CANCELED);
        finish();
    }
}
