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

    /**
     * @return true if the account exists, false if no account
     */
    public boolean created() {
        return preferences.contains(TOKEN);
    }

    /**
     * Create a new account with random data
     *
     * @throws WebServiceError on network problems
     */
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

    /**
     * Get the authentication token for talking to the web service
     *
     * @TODO make this check the expiration and get a new token as needed
     *
     * @return authentication token
     */
    public String getAuthToken() {
        throwIfNoAccount();
        return preferences.getString(TOKEN, null);
    }

    /**
     * Set the authentication token
     *
     * @param to the authentication token
     */
    public void setAuthToken(String to) {
        if (to == null || to.isEmpty()) {
            throw new AssertionError("Setting empty or null auth token");
        }
        set(TOKEN, to);
    }

    /**
     * Phone secret is actually the key used to identify the account
     *
     * @return phone secret
     */
    public String getPhoneSecret() {
        throwIfNoAccount();
        return preferences.getString(PHONE_SECRET, null);
    }

    /**
     * Set the phone secret
     *
     * @param to new phone secret
     */
    public void setPhoneSecret(String to) {
        if (to == null || to.isEmpty()) {
            throw new AssertionError("Setting empty or null phone secret");
        }
        set(PHONE_SECRET, to);
    }

    /**
     * Set the user's nickname
     *
     * @param to user's nickname
     */
    public void setNickname(String to) {
        set(NICKNAME, to);
    }

    /**
     * @return the user's nickname
     */
    public String getNickname() {
        throwIfNoAccount();
        return preferences.getString(NICKNAME, null);
    }

    /**
     * Set user's email
     *
     * @param to user's email
     */
    public void setEmail(String to) {
        set(EMAIL, to);
    }

    /**
     * @return user's email
     */
    public String getEmail() {
        throwIfNoAccount();
        return preferences.getString(EMAIL, "");
    }

    /**
     * @param to URL of the thumbnail version of the profile picture
     */
    public void setProfilePicThumbUrl(String to) {
        set(PROFILE_PIC_THUMB_URL, to);
    }

    /**
     * @return URL of the thumbnail version of the profile picture
     */
    public String getProfilePicThumbUrl() {
        throwIfNoAccount();
        return preferences.getString(PROFILE_PIC_THUMB_URL, "");
    }

    /**
     * @return currency ID for the currency this device will use
     */
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

    /**
     * Change the currency that this device will attempt to use for
     * all transactions.
     *
     * @param to currency ID to set
     */
    public void setDefaultCurrency(int to) {
        set(DEFAULT_CURRENCY, Integer.toString(to));
    }

    /**
     * Generate a phone secret
     *
     * @return random String to use as a key
     */
    private String generatePhoneSecret(){
        return getRandomString(TapApplication.integer(R.string.PHONE_SECRET_SIZE));
    }

    /**
     * Generate a random string of the requested length
     *
     * @param sizeOfRandomString # of characters in returned string
     * @return Random string of requested length
     */
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
