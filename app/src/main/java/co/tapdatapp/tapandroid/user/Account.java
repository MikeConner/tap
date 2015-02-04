/**
 * Start organizing user information into an Account class
 */

package co.tapdatapp.tapandroid.user;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.localdata.UserBalance;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.UserAccountCodex;
import co.tapdatapp.tapandroid.remotedata.WebServiceError;

public class Account {

    public final static String PREFERENCES = "CurrentUser";

    private final static String TOKEN = "AuthToken";
    private final static String DEFAULT_CURRENCY = "DefaultCurrency";
    private final static String PHONE_SECRET = "PhoneSecret";
    private final static String NICKNAME = "NickName";
    private final static String EMAIL = "eMail";
    private final static String PROFILE_PIC_THUMB_URL = "ProfilePicThumbURL";

    private SharedPreferences preferences;

    public Account() {
        super();
        preferences = TapApplication.get().getSharedPreferences(
            PREFERENCES,
            Context.MODE_PRIVATE
        );
    }

    public boolean created() {
        return preferences.contains(TOKEN);
    }

    public void createNew() throws WebServiceError {
        String phoneSecret = generatePhoneSecret();
        UserAccountCodex codex = new UserAccountCodex();
        JSONObject request = codex.marshallCreateRequest(phoneSecret);
        HttpHelper http = new HttpHelper();
        JSONObject response = http.HttpPostJSON(
            http.getFullUrl(R.string.ENDPOINT_REGISTRATION),
            new Bundle(),
            request
        );
        try {
            setPhoneSecret(phoneSecret);
            setNickname(codex.getNickname(response));
            setAuthToken(codex.getAuthToken(response));
        }
        catch (JSONException je) {
            throw new WebServiceError(je);
        }
    }

    public String getAuthToken() {
        throwIfNoAccount();
        return preferences.getString(TOKEN, null);
    }

    public void setAuthToken(String to) {
        if (to == null || to.isEmpty()) {
            throw new AssertionError("Setting empty or null auth token");
        }
        set(TOKEN, to);
    }

    public String getPhoneSecret() {
        throwIfNoAccount();
        return preferences.getString(PHONE_SECRET, null);
    }

    public void setPhoneSecret(String to) {
        if (to == null || to.isEmpty()) {
            throw new AssertionError("Setting empty or null phone secret");
        }
        set(PHONE_SECRET, to);
    }

    public void setNickname(String to) {
        set(NICKNAME, to);
    }

    public String getNickname() {
        throwIfNoAccount();
        return preferences.getString(NICKNAME, null);
    }

    public void setEmail(String to) {
        set(EMAIL, to);
    }

    public String getEmail() {
        throwIfNoAccount();
        return preferences.getString(EMAIL, "");
    }

    public void setProfilePicThumbUrl(String to) {
        set(PROFILE_PIC_THUMB_URL, to);
    }

    public String getProfilePicThumbUrl() {
        throwIfNoAccount();
        return preferences.getString(PROFILE_PIC_THUMB_URL, "");
    }

    public int getDefaultCurrency() {
        if (preferences.contains(DEFAULT_CURRENCY)) {
            return Integer.parseInt(
                preferences.getString(
                    DEFAULT_CURRENCY,
                    Integer.toString(UserBalance.CURRENCY_BITCOIN)
                )
            );
        }
        return UserBalance.CURRENCY_BITCOIN;
    }

    public void setDefaultCurrency(int to) {
        set(DEFAULT_CURRENCY, Integer.toString(to));
    }

    private String generatePhoneSecret(){
        return getRandomString(TapApplication.integer(R.string.PHONE_SECRET_SIZE));
    }

    public static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder();
        final char[] ALLOWED_CHARACTERS = TapApplication.charArray(
            R.string.PHONE_SECRET_ALLOWED_CHARACTERS
        );
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS[random.nextInt(ALLOWED_CHARACTERS.length)]);
        return sb.toString();
    }

    /**
     * Throw AssertionError if account not initialized
     */
    private void throwIfNoAccount() {
        if (!created()) {
            throw new AssertionError("Account not created");
        }
    }

    /**
     * Simplify setting a value in the preference editor
     *
     * @param key set this pair
     * @param value set to this value
     */
    private void set(String key, String value) {
        SharedPreferences.Editor prefEditor = preferences.edit();
        prefEditor.putString(key, value);
        prefEditor.apply();
    }
}
