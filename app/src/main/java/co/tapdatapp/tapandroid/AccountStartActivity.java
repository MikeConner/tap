package co.tapdatapp.tapandroid;

import android.app.Activity;
import android.os.Bundle;

import co.tapdatapp.tapandroid.remotedata.NewAccountTask;

public class AccountStartActivity extends Activity {

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

    public void newAccountComplete() {
        finish();
    }
}
