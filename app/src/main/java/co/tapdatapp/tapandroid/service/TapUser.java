package co.tapdatapp.tapandroid.service;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import co.tapdatapp.tapandroid.user.Account;

public class TapUser {

    private String mOutboundBTCaddress;
    private String mInboundBTCaddress;
    private String mAuthToken;
    private Map<String, String> mtagMap;
    private TapCloud mTapCloud;

    private String mProfilePicFull;
    private String mInboundQRcodePicture;

    public String getNewNickname(String auth_token){
        mAuthToken = auth_token;
        JSONObject user = new JSONObject();
        JSONObject json = new JSONObject();
        JSONObject output;

        String mURL = TapCloud.TAP_USERNICK_API_ENDPOINT_URL + "?auth_token=" + mAuthToken;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        try {
            user.put("auth_token", mAuthToken);
            json.put("user", user);
            //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?
            output = mTapCloud.httpPut(mURL, json);
            return output.getJSONObject("response").getString("nickname");
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
            return "";
        }
    }

    public Map<String, String> myTagHash(){
        return mtagMap;
    }

    public Map<String, String> getTags(String auth_token){
        mAuthToken = auth_token;
        String mURL = TapCloud.TAP_TAGS_API_ENDPOINT_URL + "?auth_token=" + mAuthToken;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        JSONObject output = new JSONObject();
        try {
            output = mTapCloud.httpGet(mURL);
            JSONArray jsonTags = output.getJSONArray("response");
            int length = jsonTags.length();

            mtagMap = new HashMap<String, String>();

            for (int i = 0; i < length; i++) {
                mtagMap.put(jsonTags.getJSONObject(i).getString("id"), jsonTags.getJSONObject(i).getString("name"));
            }


        }
        catch (Exception e)
        {
            //TODO: any errors possible here?
        }
        return mtagMap;
    }


    public void UpdateUser(String auth_token){
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        //END

        mAuthToken = auth_token;

        JSONObject user = new JSONObject();
        JSONObject json = new JSONObject();
        JSONObject output;
        try {
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
            output = mTapCloud.httpPut(TapCloud.TAP_USER_API_ENDPOINT_URL + ".json?auth_token=" + mAuthToken, json);
//            mAuthToken = output.getJSONObject("response").getString("auth_token");
//            mNickName = output.getJSONObject("response").getString("nickname");

        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }
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
