package co.tapdatapp.tapandroid.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by arash on 9/28/14.
 */
public class TapUser {
    private static final int PHONE_SECRET_SIZE = 16;


    private static final String ALLOWED_CHARACTERS ="0123456789qwertyuiopasdfghjklzxcvbnm";

    private String mUserEmail;
    private String mNickName;
    private String mOutboundBTCaddress;
    private int mBalance;
    private String mPhoneSecret;
    private String mInboundBTCaddress;
    private String mAuthToken;
    private Map<String, String> mtagMap;
    private TapCloud mTapCloud;

    private String mProfilePicThumb;
    private String mProfilePicFull;
    private int mTxnCount =0;
    private String mInboundQRcodePicture;

    private ArrayList<TapTxn> mTapTxns = new ArrayList<TapTxn>();


    public int getSatoshiBalance()
    {
        return mBalance;
    }
    public int TxnCount() {
        if (mTapTxns == null){
            return 0;
        }
        else {
            return mTapTxns.size();
        }
    }



    public String CreateUser (String phone_secret){
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        //END

        mPhoneSecret = phone_secret;
        JSONObject user = new JSONObject();
        JSONObject json = new JSONObject();
        JSONObject output;
        try {
            user.put("phone_secret_key", phone_secret);
            json.put("user", user);
            //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?
            output = mTapCloud.httpPost(TapCloud.TAP_REGISTER_API_ENDPOINT_URL, json);
            mAuthToken = output.getJSONObject("response").getString("auth_token");
            mNickName = output.getJSONObject("response").getString("nickname");
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }
        return mAuthToken;
    }
//

    public ArrayList<TapTxn> myTransactions(){
        return mTapTxns;
    }

    public void loadTxns(String auth_token){
        mTapTxns.clear();
    mAuthToken = auth_token;
    String mURL = TapCloud.TAP_TXN_API_ENDPOINT_URL + "?auth_token=" + mAuthToken;
    //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
    mTapCloud = new TapCloud();
    JSONObject output;
    try {
        output = mTapCloud.httpGet(mURL);
        mTxnCount = output.getInt("count");
        JSONArray jsonTXNS =  output.getJSONArray("response");
        int length = jsonTXNS.length();

        for (int i = 0; i < length; i++) {
            TapTxn tap_txn = new TapTxn();
            tap_txn.setTXNid(jsonTXNS.getJSONObject(i).getString("id"))    ;
            tap_txn.setPayloadImage(jsonTXNS.getJSONObject(i).getString("payload_image"));
            tap_txn.setUserName(jsonTXNS.getJSONObject(i).getString("other_user_nickname"));
            tap_txn.setTXNamountUSD((float) jsonTXNS.getJSONObject(i).getInt("dollar_amount") / 100);
            tap_txn.setPayloadImageThumb(jsonTXNS.getJSONObject(i).getString("payload_thumb"));
            tap_txn.setUserThumb(jsonTXNS.getJSONObject(i).getString("other_user_thumb"));
            tap_txn.setMessage(jsonTXNS.getJSONObject(i).getString("comment"));
            tap_txn.setTxnDate(jsonTXNS.getJSONObject(i).getString("date"));
            tap_txn.setTXNamountSatoshi(jsonTXNS.getJSONObject(i).getInt("satoshi_amount"));


            mTapTxns.add(tap_txn);
           // mtagMap.put(jsonTags.getJSONObject(i).getString("id"), jsonTags.getJSONObject(i).getString("name"));
        }



    }
    catch (Exception e)
    {
        e.printStackTrace();
        Log.e("error:", "" + e);
        //TODO: any errors possible here?
    }
}
    public void getBalance(String auth_token){
        mAuthToken = auth_token;
        String mURL = TapCloud.TAP_USERBALANCE_API_ENDPOINT_URL + "?auth_token=" + mAuthToken;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        JSONObject output;
        try {
            output = mTapCloud.httpGet(mURL);

            //TODO: Check to see if balances are empty.
            //TODO: load in balances from JSON Array

          //  mNickName = output.getJSONObject("response").getString("nickname");
          //  mInboundBTCaddress = output.getJSONObject("response").getString("inbound_btc_address");
          //  mOutboundBTCaddress = output.getJSONObject("response").getString("outbound_btc_address");
          //  mBalance = output.getJSONObject("response").getInt("satoshi_balance");
          //  mUserEmail = output.getJSONObject("response").getString("email");
          //  mProfilePicFull = output.getJSONObject("response").getString("profile_image");
          //  mProfilePicThumb = output.getJSONObject("response").getString("profile_thumb");
          //  mInboundQRcodePicture = output.getJSONObject("response").getJSONObject("inbound_btc_qrcode").getJSONObject("inbound_btc_qrcode").getString("url");
            //for debuggin'
            // String b = "bob";

        }
        catch (Exception e)
        {
            //TODO: any errors possible here?
            Log.e(e.toString(),e.toString());
        }

    }
    public void LoadUser(String auth_token){
        mAuthToken = auth_token;
        String mURL = TapCloud.TAP_USER_API_ENDPOINT_URL + "?auth_token=" + mAuthToken;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        JSONObject output;
        try {
            output = mTapCloud.httpGet(mURL);
            mNickName = output.getJSONObject("response").getString("nickname");
            mInboundBTCaddress = output.getJSONObject("response").getString("inbound_btc_address");
            mOutboundBTCaddress = output.getJSONObject("response").getString("outbound_btc_address");
            mBalance = output.getJSONObject("response").getInt("satoshi_balance");
            mUserEmail = output.getJSONObject("response").getString("email");
            mProfilePicFull = output.getJSONObject("response").getString("profile_image");
            mProfilePicThumb = output.getJSONObject("response").getString("profile_thumb");
            mInboundQRcodePicture = output.getJSONObject("response").getJSONObject("inbound_btc_qrcode").getJSONObject("inbound_btc_qrcode").getString("url");
            //for debuggin'
           // String b = "bob";

        }
        catch (Exception e)
        {
            //TODO: any errors possible here?
            Log.e(e.toString(),e.toString());
        }
    }

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
            mNickName = output.getJSONObject("response").getString("nickname");

        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }
        return mNickName;
    }

    //TODO: write this function - Used when Auth token expires
    public String getNewAuthToken(String phone_secret){
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        //END

        mPhoneSecret = phone_secret;
        JSONObject user = new JSONObject();
        JSONObject json = new JSONObject();
        JSONObject output;
        try {
            user.put("phone_secret_key", phone_secret);
            json.put("user", user);
            //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?

            //TODO: Update this to session controller instead of registration controller
            output = mTapCloud.httpPost(TapCloud.TAP_REGISTER_API_ENDPOINT_URL, json);
            mAuthToken = output.getJSONObject("response").getString("auth_token");
            mNickName = output.getJSONObject("response").getString("nickname");
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }
        return mAuthToken;
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
            if (mUserEmail.equals("")){
//                user.put("email", "your@email.addy");
            }else {

                user.put("email", mUserEmail);
            }

            user.put("name", mNickName);
            user.put("outbound_btc_address", mOutboundBTCaddress);
            user.put("mobile_profile_image_url", mProfilePicFull);
            user.put("mobile_profile_thumb_url", mProfilePicThumb);


            json.put("user", user);
            //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?
            output = mTapCloud.httpPut(TapCloud.TAP_USER_API_ENDPOINT_URL + ".json?auth_token=" + mAuthToken, json);
//            mAuthToken = output.getJSONObject("response").getString("auth_token");
//            mNickName = output.getJSONObject("response").getString("nickname");
            Log.e("bob", "bob");
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }
    }

    public String getProfilePicThumb(){

        return mProfilePicThumb;
    }
    public void setProfilePicThumb(String new_value){
        mProfilePicThumb = new_value;
    }


    public String getProfilePicFull(){

        return mProfilePicFull;
    }
    public void setProfilePicFull(String new_value){
        mProfilePicFull  = new_value;

    }




    public String getNickname(){

        return mNickName;
    }
    public void setNickName(String mNewNickname){
        mNickName = mNewNickname;
    }


    public String getQR() {
        return mInboundQRcodePicture;
    }

    public String getEmail(){
        return mUserEmail;
    }
    public void setEmail(String mNewEmail){
        mUserEmail = mNewEmail;
    }
    public String getBTCinbound(){
        return mInboundBTCaddress;
    }
    public void setBTCinbound(String mNewBTCinBound){
        mInboundBTCaddress = mNewBTCinBound;
    }
    public String getBTCoutbound(){
        return mOutboundBTCaddress;
    }
    public void setBTCoutbound(String mNewBTCoutbound){
        mOutboundBTCaddress = mNewBTCoutbound;
    }






    public String generatePhoneSecret(){
        mPhoneSecret =  getRandomString(PHONE_SECRET_SIZE);
        return mPhoneSecret;
    }

    public static String getRandomString(final int sizeOfRandomString)
    {
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder();
        for(int i=0;i<sizeOfRandomString;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }
}
