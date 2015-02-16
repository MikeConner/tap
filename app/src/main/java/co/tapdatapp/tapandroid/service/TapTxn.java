package co.tapdatapp.tapandroid.service;

import org.json.JSONException;
import org.json.JSONObject;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;

public class TapTxn {

    private final HttpHelper httpHelper;

    private String mTXNid = "";
    private String mPayloadURL = "";
    private String mUserNickname ="";
    private String mPayloadImage = "";
    private float mAmountUSD = 0;
    private int mAmountSatoshi = 0;
    private String mPayloadImageThumb = "";
    private String mUserImageThumb = "";



    private String mTagID;
    private TapCloud mTapCloud;
    private int mSatoshi;
    private String mMessage;
    private String mTxnDate;
    private int mEndingUserBalanaceSatoshi = 0;

    private String mAuthToken;

    private float mTxnAmount;

    public TapTxn() {
        httpHelper = new HttpHelper();
    }

    public void setTxnAmount(float new_amount){
        mTxnAmount = new_amount;
    }
    public void setTagID(String new_value){
        mTagID = new_value;
    }
    public void setAuthToken(String new_value){
        mAuthToken = new_value;
    }

    public String getTxnDate(){
        return mTxnDate;
    }
    public void setTxnDate(String new_value){
        mTxnDate = new_value;
    }


    public String getUserThumb(){
        return mUserImageThumb;
    }
    public void setUserThumb(String new_value){
        mUserImageThumb = new_value;
    }


    public String getPayloadImageThumb(){
        return mPayloadImageThumb;
    }
    public void setPayloadImageThumb(String new_value){
        mPayloadImageThumb = new_value;
    }



    public int getTXNamountSatoshi(){
        return mAmountSatoshi;
    }
    public void setTXNamountSatoshi(int new_value){
        mAmountSatoshi = new_value;
    }


    public float getTXNamountUSD(){
        return mAmountUSD;
    }
    public void setTXNamountUSD(float new_value){
        mAmountUSD = new_value;
    }


    public String getPayloadImage(){
        return mPayloadImage;
    }
    public void setPayloadImage(String new_value){
        mPayloadImage = new_value;
    }



    public String getUserName(){
        return mUserNickname;
    }
    public void setUserName(String new_value){
        mUserNickname = new_value;
    }

    public String getTXNid(){
        return mTXNid;
    }
    public void setTXNid(String new_value){
        mTXNid = new_value;
    }

    public void setPayLoadURL(String new_value){
        mPayloadURL = new_value;
    }

    public String getPayloadURL(){
        return mPayloadURL;

    }
    public String getMessage(){
        return mMessage;
    }
    public void setMessage(String new_value){
        mMessage = new_value;
    }
    public int getSatoshi(){
        return mSatoshi;
    }


    public void TapAfool() throws JSONException {

        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        //END

        if (mAuthToken != null && mTagID != null && mTxnAmount != 0){

            JSONObject json = new JSONObject();
            JSONObject output;
            json.put("auth_token", mAuthToken);
            json.put("tag_id", mTagID   );
            json.put("amount", mTxnAmount);


            //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?
            output = mTapCloud.httpPost(httpHelper.getFullUrl(R.string.ENDPOINT_TRANSACTION), json);
            mSatoshi = output.getJSONObject("response").getInt("satoshi_amount");
            mAmountUSD = (float) (output.getJSONObject("response").getInt("dollar_amount") / 100);
            mEndingUserBalanaceSatoshi = output.getJSONObject("response").getInt("final_balance");
            mUserImageThumb = output.getJSONObject("response").getString("tapped_user_thumb");
            mUserNickname = output.getJSONObject("response").getString("tapped_user_name");

            mPayloadURL = output.getJSONObject("response").getJSONObject("payload").getString("uri");
            mMessage = output.getJSONObject("response").getJSONObject("payload").getString("text");
            mPayloadImageThumb = output.getJSONObject("response").getJSONObject("payload").getString("thumb");
            mPayloadImage = output.getJSONObject("response").getJSONObject("payload").getString("image");

//            /{"response":{"satoshi":936593,"payload":{"uri":"https:\/\/s3.amazonaws.com\/tapyapa\/new_key_needed","text":"Enter Your message here"}}}

        }
    }

}
