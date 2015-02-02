package co.tapdatapp.tapandroid.remotedata;

import android.os.Bundle;
import android.test.AndroidTestCase;
import android.util.Log;

import org.json.JSONObject;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.user.Account;

public class HttpHelperTest extends AndroidTestCase {

    /**
     * jack the transactions listing to test the HTTP worker code.
     * Note that at this time, the authentication is wonky, and
     * this test is only good if you first run the app on the testing
     * device to create an account token there.
     *
     * @throws Exception
     */
    public void testTransactionRequest() throws Exception {
        Account a = new Account();
        if (!a.created()) {
            fail("Empty auth token");
        }
        HttpHelper http = new HttpHelper();
        JSONObject response = http.HttpGetJSON(
            http.getFullUrl(R.string.ENDPOINT_TRANSACTION_LIST),
            new Bundle()
        );
        Log.d("TEST", "Test successful");
        Log.d("TEST", response.toString());
    }
}
