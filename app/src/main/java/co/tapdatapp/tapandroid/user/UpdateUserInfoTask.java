/**
 * Background task to update the user's information on the server
 */

package co.tapdatapp.tapandroid.user;

import android.os.AsyncTask;
import android.os.Bundle;

import org.json.JSONObject;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;

public class UpdateUserInfoTask extends AsyncTask<Void, Void, Void> {

    private Throwable error = null;

    @Override
    protected Void doInBackground(Void... params) {
        try {
            updateUser();
        }
        catch (Throwable t) {
            error = t;
        }
        return null;
    }

    /**
     * Handles any error resulting from the background task. Since
     * this operation is more detached from the front-end than usual,
     * just call an error handler from here.
     */
    @Override
    protected void onPostExecute(Void x) {
        if (error != null) {
            TapApplication.handleFailures(error);
        }
    }

    /**
     * Marshall all local user information and push it to the server
     * as an update.
     *
     * @throws Exception on various errors (mostly network-related)
     */
    public void updateUser() throws Exception {
        JSONObject user = new JSONObject();
        JSONObject json = new JSONObject();
        Account account = new Account();
        user.put("email", account.getEmail());
        user.put("name", account.getNickname());
        user.put("mobile_profile_thumb_url", account.getProfilePicThumbUrl());
        json.put("user", user);
        HttpHelper httpHelper = new HttpHelper();
        httpHelper.HttpPutJSON(
            httpHelper.getFullUrl(R.string.ENDPOINT_USER_API),
            new Bundle(),
            json
        );
    }
}
