package co.tapdatapp.tapandroid.remotedata;

import org.json.JSONException;
import org.json.JSONObject;

public class UserAccountCodex {

    private final static String PHONE_SECRET_KEY = "phone_secret_key";
    private final static String CREATE_USER_KEY = "user";
    private final static String RESPONSE = "response";
    private final static String AUTH_TOKEN = "auth_token";
    private final static String NICKNAME = "nickname";

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

    public String getAuthToken(JSONObject httpResponse)
    throws JSONException {
        return httpResponse.getJSONObject(RESPONSE).getString(AUTH_TOKEN);
    }

    public String getNickname(JSONObject httpResponse)
    throws JSONException {
        return httpResponse.getJSONObject(RESPONSE).getString(NICKNAME);
    }

}
