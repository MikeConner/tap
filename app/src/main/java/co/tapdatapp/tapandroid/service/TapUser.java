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
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.WebResponse;
import co.tapdatapp.tapandroid.remotedata.WebServiceError;
import co.tapdatapp.tapandroid.user.Account;

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
                    TapApplication.unknownFailure(je);
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
            TapApplication.unknownFailure(e);
            throw new WebServiceError(e);
        }
    }


    public void UpdateUser(String auth_token) throws JSONException {
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        //END

        mAuthToken = auth_token;

        JSONObject user = new JSONObject();
        JSONObject json = new JSONObject();
        JSONObject output;
        Account account = new Account();
        if (account.getEmail().isEmpty()){
//                user.put("email", "your@email.addy");
        } else {
            user.put("email", account.getEmail());
        }

        user.put("name", new Account().getNickname());
        user.put("outbound_btc_address", mOutboundBTCaddress);
        user.put("mobile_profile_image_url", mProfilePicFull);
        user.put("mobile_profile_thumb_url", new Account().getProfilePicThumbUrl());


        json.put("user", user);
        //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?
        output = mTapCloud.httpPut(httpHelper.getFullUrl(R.string.ENDPOINT_USER_API), json);
//            mAuthToken = output.getJSONObject("response").getString("auth_token");
//            mNickName = output.getJSONObject("response").getString("nickname");

    }

    public void setProfilePicFull(String new_value){
        mProfilePicFull  = new_value;

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
