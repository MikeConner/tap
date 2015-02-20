package co.tapdatapp.tapandroid.service;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;

public class TapYapa {
    private String mURL;
    private String mContent;
    private int mThreshold;
    private String mYapaID;
    private TapCloud mTapCloud;
    private String mYapaURL;
    private String mThumbnailURL;
    private String mAuthToken;

    private HttpHelper httpHelper;

    public TapYapa() {
        httpHelper = new HttpHelper();
    }

    public void
    loadYapa(String auth_token, String yapa_id, String tag_id)
    throws JSONException {
        HashMap<String, String> params = new HashMap<>();
        params.put("tag_id", tag_id);
        String url = httpHelper.getFullUrl(R.string.ENDPOINT_TAP_ONE_YAPA, yapa_id + ".json", params);
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        JSONObject output = mTapCloud.httpGet(url);
        mURL = output.getJSONObject("response").getString("uri");
        mContent = output.getJSONObject("response").getString("text");
        mThreshold = output.getJSONObject("response").getInt("threshold");
        mYapaURL = output.getJSONObject("response").getString("payload_image");
        mThumbnailURL = output.getJSONObject("response").getString("payload_thumb");
    }

    public String getFullYapa(){
        return mYapaURL;
    }
    public void setFullYapa(String new_value){
        mYapaURL=new_value;
    }

    public String getThumbYapa(){
        return mThumbnailURL;
    }
    public void setThumbYapa(String new_value){
        mThumbnailURL=new_value;
    }



    public String getURL(){
        return mURL;
    }
    public void setURL(String new_value){
        mURL=new_value;
    }

    public String getContent(){
        return mContent;
    }
    public void setContent(String new_value){
        mContent=new_value;
    }
    public String getYapaID(){
        return mYapaID;
    }
    public void setYapaID(String new_value){
        mYapaID=new_value;
    }

    public int getThreshold(){
        return mThreshold;
    }
    public void setThreshold(int new_value){
        mThreshold=new_value;
    }

    public void updateYapa(String auth_token, String tag_id) throws JSONException {
        mAuthToken = auth_token;

        JSONObject json = new JSONObject();
        JSONObject payload = new JSONObject();

        JSONObject output;
        HashMap<String, String> params = new HashMap<>();
        params.put("tag_id", tag_id);
        String url = httpHelper.getFullUrl(R.string.ENDPOINT_TAP_ONE_YAPA, mYapaID + ".json", params);
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();

        payload.put("threshold", mThreshold);
        payload.put("content", mContent);
        payload.put("uri", mURL);
        payload.put("mobile_payload_image_url", mYapaURL);
        payload.put("mobile_payload_thumb_url", mThumbnailURL);

        json.put("auth_token", mAuthToken);
        json.put("id", mYapaID);
        json.put("tag_id", tag_id);
        json.put("payload", payload);



        //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?
        output = mTapCloud.httpPut(url, json);
        // = output.getJSONObject("response").getString("nickname");
        //CHECK FOR BAD CASES HERE!

    }
}

