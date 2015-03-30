package co.tapdatapp.tapandroid.service;

import android.os.Bundle;

import org.json.JSONObject;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.user.Account;

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
    private int mCurrencyId;

    private String slug;
    private String mTagID;
    private String mMessage;
    private String mTxnDate;
    private int mEndingUserBalanaceSatoshi;

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

    public void setCurrencyId(int to) {
        mCurrencyId = to;
    }

    public void TapAfool() throws Exception {

        if (mTagID == null) {
            throw new AssertionError("tag id == null");
        }
        if (mTxnAmount == 0) {
            throw new AssertionError("transaction amount == 0");
        }

        JSONObject json = new JSONObject();
        JSONObject output;
        json.put("auth_token", new Account().getAuthToken());
        json.put("tag_id", mTagID   );
        json.put("amount", mTxnAmount);
        if (mCurrencyId != CurrencyDAO.CURRENCY_BITCOIN) {
            json.put("currency_id", mCurrencyId);
        }


        /*

        works for current case (bTC)

        needs to consume this:

        {"response":
            {
                "tapped_user_thumb":{"profile_thumb":{"url":null}},
                "amount":1,
                "payload":{"image":"","text":"Tapped!","content_type":"image","thumb":"","uri":""},
                "currency_id":5,
                "final_balance":98,
                "tapped_user_name":"Demo Account",
                "dollar_amount":null


            }
        }

        */

        output = httpHelper.HttpPostJSON(httpHelper.getFullUrl(R.string.ENDPOINT_TRANSACTION), new Bundle(), json);
        try {
            mAmountUSD = (float) (output.getJSONObject("response").getInt("dollar_amount") / 100);
        }
        catch (Exception e){
            if(output.getJSONObject("response").get("dollar_amount").equals(null)) {
                //we're in a currency txn / no $ associated
            }
            else {
                throw new AssertionError("Failed to get dollar_amount and it's not null");
            }
        }
        slug = output.getJSONObject("response").getString("slug");
        mEndingUserBalanaceSatoshi = output.getJSONObject("response").getInt("final_balance");
        mUserImageThumb = output.getJSONObject("response").getString("tapped_user_thumb");
        mUserNickname = output.getJSONObject("response").getString("tapped_user_name");

        mPayloadURL = output.getJSONObject("response").getJSONObject("payload").getString("uri");
        mMessage = output.getJSONObject("response").getJSONObject("payload").getString("text");
        mPayloadImageThumb = output.getJSONObject("response").getJSONObject("payload").getString("thumb");
        mPayloadImage = output.getJSONObject("response").getJSONObject("payload").getString("image");

//      {"response":{"satoshi":936593,"payload":{"uri":"https:\/\/s3.amazonaws.com\/tapyapa\/new_key_needed","text":"Enter Your message here"}}}

        // Expire local balances so they are refreshed from the server
        new Account().expireBalances();
    }

    public String getSlug() {
        return slug;
    }
}
