package co.tapdatapp.tapandroid.service;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by arash on 9/28/14.
 */
public class TapTag {
    private String mTagID;
    private String mTagName;
    private String mAuthToken;
    private TapCloud mTapCloud;
    private ArrayList<TapYapa> mTapYapas = new ArrayList<TapYapa>();


    public ArrayList<TapYapa> myYappas(){
        return mTapYapas;
    }

    public int YapaCount() {
        if (mTapYapas == null){
            return 0;
        }
        else {
            return mTapYapas.size();
        }

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
                json.put("tag_id", mTagID.replaceAll("-",""));
                json.put("payload", payload);
                //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?
                output = mTapCloud.httpPost(TapCloud.TAP_YAPA_API_ENDPOINT_URL, json);
                mYapa.setYapaID( output.getString("response"));
                String J = "dslkfj;";
//                mAuthToken = output.getJSONObject("response").getString("auth_token");
 //               mNickName = output.getJSONObject("response").getString("nickname");
            }
            catch (JSONException e) {
                e.printStackTrace();
                Log.e("JSON", "" + e);
            }

        }
        else
        {
            mYapa.loadYapa(auth_token, mYapa.getYapaID(), mTagID.replaceAll("-",""));
            //load yappa from web

        }
        mTapYapas.add(mYapa);



    }
    public void loadYapa(String auth_token){
        mAuthToken = auth_token;
        String mURL = TapCloud.TAP_YAPA_API_ENDPOINT_URL + "?auth_token=" + mAuthToken + "&tag_id=" + mTagID.replaceAll("-","");
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        JSONObject output;
        try {
            output = mTapCloud.httpGet(mURL);
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



    public void updateTag(String auth_token, String tag_id, String new_name){
        mAuthToken = auth_token;

        JSONObject json = new JSONObject();
        JSONObject output;

        String mURL = TapCloud.TAP_TAG_API_ENDPOINT_URL + "?auth_token=" + mAuthToken;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();

        try {

            json.put("auth_token", mAuthToken);
            json.put("id", "0");
            json.put("name", new_name);
            json.put("tag_id", tag_id);

            //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?
            output = mTapCloud.httpPut(mURL, json);
            // = output.getJSONObject("response").getString("nickname");
            //CHECK FOR BAD CASES HERE!
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }

    }
    //TODO: this does not work currently! Not really needed since we're passing everything in manually to the view
    public void loadTag(String auth_token, String tag_id){
        mAuthToken=auth_token;
        mTagID = tag_id;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        //END


        String mURL = TapCloud.TAP_TAGS_API_ENDPOINT_URL + "?auth_token=" + mAuthToken;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        JSONObject output;
        try {
            output = mTapCloud.httpGet(mURL);
                //TODO: get individual tag load here? (with count etc?) + any yapa it may have?
//            mTagName = output.getJSONObject("response").getString("name");
//            mInboundBTCaddress = output.getJSONObject("response").getString("inbound_btc_address");
//            mOutboundBTCaddress = output.getJSONObject("response").getString("outbound_btc_address");
//            mBalance = output.getJSONObject("response").getInt("satoshi_balance");
//            mUserEmail = output.getJSONObject("response").getString("email");
        }
        catch (Exception e)
        {
            //TODO: any errors possible here?
        }

        loadYapa(auth_token);




    }
    public String generateNewTag(String auth_token, String default_yapa_thumb){
        mAuthToken = auth_token;
        //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
        mTapCloud = new TapCloud();
        //END

        JSONObject tag   = new JSONObject();

        JSONObject output;
        try {
            tag.put("auth_token", mAuthToken);
            //json.put("user", user);
            //TODO: Assuming success, but if it fails, we need to capture that and show an error or Try again?

            //TODO: Update this to session controller instead of registration controller
            output = mTapCloud.httpPost(TapCloud.TAP_TAGS_API_ENDPOINT_URL, tag);
            mTagID = output.getJSONObject("response").getString("id");
            mTagName = output.getJSONObject("response").getString("name");
        //    Log.e(output.toString(), "" );
        }
        catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }

        //generate first Yapa!
        TapYapa new_yapa = new TapYapa();
        new_yapa.setContent("Say something funny or meaningful.  You're getting TAPPED!");
        new_yapa.setThreshold(1);
        //TODO: Make this a default no yapa image on AWS
        new_yapa.setThumbYapa(default_yapa_thumb);
        addYapa(mAuthToken, new_yapa);

        return mTagID;
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
