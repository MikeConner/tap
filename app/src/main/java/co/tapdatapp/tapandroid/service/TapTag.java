package co.tapdatapp.tapandroid.service;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.UserFriendlyError;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.WebServiceError;

public class TapTag {
    private HttpHelper httpHelper;
    private String mTagID;
    private String mTagName;
    private String mAuthToken;
    private TapCloud mTapCloud;
    private ArrayList<TapYapa> mTapYapas = new ArrayList<>();

    public TapTag() {
        httpHelper = new HttpHelper();
    }

    public ArrayList<TapYapa> myYappas(){
        return mTapYapas;
    }

    public void addYapa(String auth_token, TapYapa mYapa)
    {
        if(mYapa.getYapaID() == null){
            //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
            if(mTapCloud == null){ mTapCloud = new TapCloud();}
            //END

            mAuthToken = auth_token;
            JSONObject payload = new JSONObject();
            JSONObject json = new JSONObject();
            JSONObject output;
            try {
                payload.put("uri", mYapa.getURL());
                payload.put("content", mYapa.getContent());
                payload.put("threshold", mYapa.getThreshold());
                payload.put("mobile_payload_image_url", mYapa.getFullYapa());
                payload.put("mobile_payload_thumb_url", mYapa.getThumbYapa());

                json.put("auth_token", mAuthToken);
                json.put("tag_id", mTagID.replaceAll("-", ""));
                json.put("payload", payload);
                //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?
                output = httpHelper.HttpPostJSON(httpHelper.getFullUrl(R.string.ENDPOINT_YAPA), new Bundle(), json);
                mYapa.setYapaID(output.getString("response"));
            }
            catch (Exception e) {
                addYapaErrors(e);
            }
        }
        else
        {
            try {
                mYapa.loadYapa(auth_token, mYapa.getYapaID(), mTagID.replaceAll("-", ""));
                //load yappa from web
            }
            catch (JSONException je) {
                addYapaErrors(je);
            }
        }
        mTapYapas.add(mYapa);
    }

    public void loadYapa(String auth_token){
        mAuthToken = auth_token;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        JSONObject output;
        try {
            output = mTapCloud.httpGet(
                httpHelper.getFullUrl(R.string.ENDPOINT_TAP_ONE_YAPA) +
                "&tag_id=" + mTagID.replaceAll("-","")
            );
            int tag_count = output.getInt("count");
            if (tag_count != 0 ){
                JSONArray ja =  output.getJSONArray("response");
                for(int i=0; i<ja.length(); i++){
                    TapYapa yap = new TapYapa();
                    yap.setYapaID(ja.get(i).toString());
                    addYapa(auth_token, yap);

                }
            }
//

        }
        catch (Exception e)
        {
            Log.e(e.toString(),"b"); //TODO: any errors possible here?
        }

    }



    public void
    updateTag(String auth_token, String tag_id, String new_name)
    throws JSONException {
        mAuthToken = auth_token;

        JSONObject json = new JSONObject();
        JSONObject output;

        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();

        json.put("auth_token", mAuthToken);
        json.put("id", "0");
        json.put("name", new_name);
        json.put("tag_id", tag_id);

        //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?
        output = mTapCloud.httpPut(httpHelper.getFullUrl(R.string.ENDPOINT_TAGS), json);
        // = output.getJSONObject("response").getString("nickname");
        //CHECK FOR BAD CASES HERE!

    }

    public String
    generateNewTag(String auth_token, String default_yapa_thumb)
    throws JSONException, WebServiceError {
        mAuthToken = auth_token;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        //END

        JSONObject tag   = new JSONObject();

        JSONObject output;
        tag.put("auth_token", mAuthToken);
        //json.put("user", user);
        //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?

        //TODO: Update this to session controller instead of registration controller
        output = httpHelper.HttpPostJSON(httpHelper.getFullUrl(R.string.ENDPOINT_TAGS), new Bundle(), tag);
        mTagID = output.getJSONObject("response").getString("id");
        mTagName = output.getJSONObject("response").getString("name");

        //generate first Yapa!
        TapYapa new_yapa = new TapYapa();
        new_yapa.setContent("Say something funny or meaningful.  You're getting TAPPED!");
        new_yapa.setThreshold(1);
        //TODO: Make this a default no yapa image on AWS
        new_yapa.setThumbYapa(default_yapa_thumb);
        addYapa(mAuthToken, new_yapa);

        return mTagID;
    }

    /**
     * This is to handle any common errors if adding a yapa fails
     * @param t contains data on the error
     */
    public void addYapaErrors(Throwable t){
        try{
            throw t;
        }
        catch (UserFriendlyError ufe){
            TapApplication.errorToUser(ufe);
        }
        catch(Throwable catchall){
            TapApplication.unknownFailure(t);
        }
    }

    public void setTagID(String new_value){
        mTagID = new_value;
    }
    public void setTagName (String new_value){
        mTagName = new_value;
    }
    public String getTagID(){
        return mTagID;
    }
    public String getTagName(){
        return mTagName;
    }

}
