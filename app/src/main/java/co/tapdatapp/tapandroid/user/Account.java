/**
 * Start organizing user information into an Account class
 */

package co.tapdatapp.tapandroid.user;

import android.content.Context;
import android.content.SharedPreferences;

import co.tapdatapp.tapandroid.TapApplication;

public class Account {

    public final static String PREFERENCES = "CurrentUser";

    private final static String TOKEN = "AuthToken";

    private SharedPreferences preferences;
    private SharedPreferences.Editor prefEditor;

    public Account() {
        super();
        preferences = TapApplication.get().getSharedPreferences(
            PREFERENCES,
            Context.MODE_PRIVATE
        );
        prefEditor = preferences.edit();
    }

    public boolean created() {
        return preferences.contains(TOKEN);
    }

    public String getAuthToken() {
        if (!created()) {
            throw new AssertionError("Account not created");
        }
        return preferences.getString(TOKEN, null);
    }

    public void setAuthToken(String to) {
        if (to == null || to.isEmpty()) {
            throw new AssertionError("Setting empty or null auth token");
        }
        prefEditor.putString(TOKEN, to);
        prefEditor.commit();
    }
}
