/**
 * Translate user accounts between different representations
 */

package co.tapdatapp.tapandroid.remotedata;

import org.json.JSONException;
import org.json.JSONObject;

public class UserAccountCodec {

    private final static String PHONE_SECRET_KEY = "phone_secret_key";
    private final static String CREATE_USER_KEY = "user";
    private final static String RESPONSE = "response";
    private final static String AUTH_TOKEN = "auth_token";
    private final static String NICKNAME = "nickname";

    /**
     * Create a JSON request for creating a new account
     *
     * @param phoneSecret Phone secret
     * @return JSONObject to pass on to the server
     */
    public JSONObject marshallCreateRequest(String phoneSecret) {
        JSONObject rv = new JSONObject();
        try {
            JSONObject userObj = new JSONObject();
            userObj.put(PHONE_SECRET_KEY, phoneSecret);
            rv.put(CREATE_USER_KEY, userObj);
        }
        catch (JSONException je) {
            throw new AssertionError(je);
        }
        return rv;
    }

    /**
     * Extract the auth token element from a create account response
     *
     * @param httpResponse response to a create account HTTP call
     * @return the auth token element
     * @throws JSONException if anything goes wrong
     */
    public String getAuthToken(JSONObject httpResponse)
    throws JSONException {
        return httpResponse.getJSONObject(RESPONSE).getString(AUTH_TOKEN);
    }

    /**
     * Extract the nickname portion from a create account response
     *
     * @param httpResponse response to a creat account HTTP call
     * @return the nickname element
     * @throws JSONException if anything goes wrong
     */
    public String getNickname(JSONObject httpResponse)
    throws JSONException {
        return httpResponse.getJSONObject(RESPONSE).getString(NICKNAME);
    }

}
