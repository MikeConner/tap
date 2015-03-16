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
import co.tapdatapp.tapandroid.currency.BalanceList;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.UserAccountCodec;
import co.tapdatapp.tapandroid.remotedata.WebServiceError;

public class Account {

    public final static String PREFERENCES = "CurrentUser";

    private final static String TOKEN = "AuthToken";
    private final static String DEFAULT_CURRENCY = "DefaultCurrency";
    private final static String PHONE_SECRET = "PhoneSecret";
    private final static String NICKNAME = "NickName";
    private final static String EMAIL = "eMail";
    private final static String PROFILE_PIC_THUMB_URL = "ProfilePicThumbURL";
    private final static String INBOUND_BITCOIN_ADDRESS = "InboundBTCAddress";
    private final static String BITCOIN_QR_URL = "BitcoinQRCodeUrl";

    private SharedPreferences preferences;

    // @TODO I don't think this belongs in this class ...
    private static int armedAmount = 0;

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
        UserAccountCodec codec = new UserAccountCodec();
        JSONObject request = codec.marshallCreateRequest(phoneSecret);
        HttpHelper http = new HttpHelper();
        JSONObject response = http.HttpPostJSON(
            http.getFullUrl(R.string.ENDPOINT_REGISTRATION),
            new Bundle(),
            request
        );
        try {
            setPhoneSecret(phoneSecret);
            _setNickname(codec.getNickname(response));
            setAuthToken(codec.getAuthToken(response));
            setCurrencyOnNewUser();
        }
        catch (JSONException je) {
            deleteAccount();
            throw new WebServiceError(je);
        }
        response = http.HttpGetJSON(
            http.getFullUrl(R.string.ENDPOINT_USER_API),
            new Bundle()
        );
        try {
            setBitcoinAddress(codec.getBitcoinAddress(response));
            setBitcoinQrUrl(codec.getQRCode(response));
        }
        catch (JSONException je) {
            deleteAccount();
            throw new WebServiceError(je);
        }
    }

    /**
     * Remove anything related to the account. Mainly used to clean
     * up after a 1/2 created account fails.
     */
    private void deleteAccount() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
    }

    /**
     * For a new user, set the default currency to whatever has the
     * highest balance. Remains to be seen whether this becomes a
     * production behavior, but it's probably a good idea, so that
     * any promo balance the user gets by creating a new account
     * automatically becomes the default
     */
    private void setCurrencyOnNewUser() throws WebServiceError {
        CurrencyDAO balance = new CurrencyDAO();
        BalanceList balances = balance.getAllBalances();
        // Bitcoin is a special case
        balance.ensureLocalCurrencyDetails(CurrencyDAO.CURRENCY_BITCOIN);
        balance.ensureLocalCurrencyDetails(balances);
        Integer currencyId = null;
        int highestBalance = 0;
        for (int currentId : balances.keySet()) {
            int currentBalance = balances.get(currentId);
            if (currentBalance >= highestBalance) {
                highestBalance = currentBalance;
                currencyId = currentId;
            }
        }
        if (currencyId != null) {
            setActiveCurrency(currencyId);
        }
    }

    /**
     * Get the authentication token for talking to the web service
     *
     * @return authentication token
     */
    // @TODO make this check the expiration and get a new token as needed
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
     * Set the user's inbound bitcoin address
     *
     * @param to user's inbound bitcoin address
     */
    public void setBitcoinAddress(String to) {
        if (to == null) {
            throw new AssertionError("setting bitcoin address to null");
        }
        set(INBOUND_BITCOIN_ADDRESS, to);
    }

    /**
     * @return the user's inbound bitcoin address
     */
    public String getBitcoinAddress() {
        throwIfNoAccount();
        return preferences.getString(INBOUND_BITCOIN_ADDRESS, null);
    }

    /**
     * Set the user's bitcoin QR code URL
     *
     * @param to user's nickname
     */
    public void setBitcoinQrUrl(String to) {
        if (to == null) {
            throw new AssertionError("setting bitcoin QR to null");
        }
        set(BITCOIN_QR_URL, to);
    }

    /**
     * @return the user's bitcoin QR code URL
     */
    public String getBitcoinQrUrl() {
        throwIfNoAccount();
        return preferences.getString(BITCOIN_QR_URL, null);
    }

    /**
     * Set user's email
     *
     * @param to user's email
     */
    public void setEmail(String to) {
        set(EMAIL, to);
        new UpdateUserInfoTask().execute();
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
        new UpdateUserInfoTask().execute();
    }

    /**
     * @return URL of the thumbnail version of the profile picture
     */
    public String getProfilePicThumbUrl() {
        throwIfNoAccount();
        return preferences.getString(PROFILE_PIC_THUMB_URL, null);
    }

    /**
     * @return currency ID for the currency this device will use
     */
    public int getActiveCurrency() {
        if (preferences.contains(DEFAULT_CURRENCY)) {
            return Integer.parseInt(
                preferences.getString(
                    DEFAULT_CURRENCY,
                    Integer.toString(CurrencyDAO.CURRENCY_BITCOIN)
                )
            );
        }
        return CurrencyDAO.CURRENCY_BITCOIN;
    }

    /**
     * Change the currency that this device will attempt to use for
     * all transactions.
     *
     * @param to currency ID to set
     */
    public void setActiveCurrency(int to) {
        set(DEFAULT_CURRENCY, Integer.toString(to));
    }

    /**
     * Set the current amount the system is armed to pay on tap
     *
     * @param to Amount to arm to (absolute)
     */
    // @TODO I'm not confident that this class is the right place for
    // this informaiton, but it's a decent placeholder for the time
    // being
    public void setArmedAmount(int to) {
        synchronized (Account.class) {
            armedAmount = to;
        }
    }

    /**
     * Get the amount the system is armed to pay
     *
     * @return Amount the system is armed to pay
     */
    public int getArmedAmount() {
        synchronized (Account.class) {
            return armedAmount;
        }
    }

    /**
     * Set the user's nickname _and_ save it to the server
     *
     * @param to New nickname
     */
    public void setNickname(String to) {
        _setNickname(to);
        new UpdateUserInfoTask().execute();
    }

    /**
     * Set the user's nickname without saving it back to the server
     *
     * @param to user's nickname
     */
    private void _setNickname(String to) {
        if (to == null) {
            throw new AssertionError("setting nickname to null");
        }
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
