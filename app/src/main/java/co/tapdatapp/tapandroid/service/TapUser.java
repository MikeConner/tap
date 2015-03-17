package co.tapdatapp.tapandroid.service;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.UserFriendlyError;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.WebResponse;
import co.tapdatapp.tapandroid.remotedata.WebServiceError;

public class TapUser {

    private final HttpHelper httpHelper;
    private String mOutboundBTCaddress;
    private String mInboundBTCaddress;
    private String mAuthToken;
    private Map<String, String> mtagMap;
    private TapCloud mTapCloud;

    private String mProfilePicFull;
    private String mInboundQRcodePicture;

    public TapUser() {
        httpHelper = new HttpHelper();
    }

    public String
    getNewNickname(String auth_token) throws JSONException {
        mAuthToken = auth_token;
        JSONObject user = new JSONObject();
        JSONObject json = new JSONObject();
        JSONObject output;

        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        user.put("auth_token", mAuthToken);
        json.put("user", user);
        //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?
        output = mTapCloud.httpPut(httpHelper.getFullUrl(R.string.ENDPOINT_RESET_NICK), json);
        return output.getJSONObject("response").getString("nickname");
    }

    public Map<String, String> myTagHash(){
        return mtagMap;
    }

    public Map<String, String>
    getTags(String auth_token) throws WebServiceError {
        mAuthToken = auth_token;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        JSONObject output = null;
        try {
            WebResponse wr = httpHelper.HttpGet(httpHelper.getFullUrl(R.string.ENDPOINT_TAGS), new Bundle());
            output = wr.getJSON();
            JSONArray jsonTags = new JSONArray();
            mtagMap = new HashMap<>();
            try {
                jsonTags = output.getJSONArray("response");
            }
            catch (JSONException je) {
                if (je.getMessage().startsWith("No value for")) {
                    // In case of empty return value, return empty Map
                    return mtagMap;
                }
                else {
                    mappingErrors(je);
                }
            }
            int length = jsonTags.length();

            for (int i = 0; i < length; i++) {
                mtagMap.put(jsonTags.getJSONObject(i).getString("id"), jsonTags.getJSONObject(i).getString("name"));
            }
            return mtagMap;
        }
        catch (JSONException je) {
            Log.e("WEBSERVICE", output.toString());
            throw new WebServiceError(je);
        }
        catch (Exception e) {
            mappingErrors(e);
            throw new WebServiceError(e);
        }
    }

    public void setProfilePicFull(String new_value){
        mProfilePicFull  = new_value;

    }

    /**
     * This gets called if something in the Map method fails
     * @param t contains error data.
     */
    public void mappingErrors(Throwable t){
        try{
            throw t;
        }
        catch(UserFriendlyError ufe){
            TapApplication.errorToUser(ufe);
        }
        catch(Throwable catchall){
            TapApplication.unknownFailure(t);
        }
    }

    public String getQR() {
        return mInboundQRcodePicture;
    }

    public String getBTCinbound(){
        return mInboundBTCaddress;
    }
    public String getBTCoutbound(){
        return mOutboundBTCaddress;
    }

}
