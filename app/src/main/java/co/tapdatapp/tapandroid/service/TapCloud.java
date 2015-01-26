package co.tapdatapp.tapandroid.service;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.amazonaws.util.IOUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;


//TODO: All network should be done async
//TODO: remove strict Mode

public class TapCloud {
    DefaultHttpClient client;


    //*/
    public final static String TAP_LOCAL = "192.168.1.135:3000";
    public final static String TAP_REMOTE = "www.tapdatapp.co";
    public final static String TAP_SERVER = TAP_REMOTE;


    public final static String TAP_REGISTER_API_ENDPOINT_URL = "http://" + TAP_SERVER + "/mobile/1/registrations.json";

    public final static String TAP_USER_API_ENDPOINT_URL = "http://" + TAP_SERVER + "/mobile/1/users/me";
    public final static String TAP_USERNICK_API_ENDPOINT_URL = "http://" + TAP_SERVER + "/mobile/1/users/reset_nickname";
    public final static String TAP_USERBALANCE_API_ENDPOINT_URL = "http://" + TAP_SERVER + "/mobile/1/users/balance_inquiry";
    public final static String TAP_USER_VOUCHER_API_ENDPOINT_URL = "http://" + TAP_SERVER + "/mobile/1/users/VOUCHER_CODE/redeem_voucher";


    public final static String TAP_TAGS_API_ENDPOINT_URL = "http://" + TAP_SERVER + "/mobile/1/nfc_tags.json";
    public final static String TAP_TAG_API_ENDPOINT_URL = "http://" + TAP_SERVER + "/mobile/1/nfc_tags/0.json";

    public final static String TAP_YAPA_API_ENDPOINT_URL = "http://" + TAP_SERVER + "/mobile/1/payloads.json";
    public final static String TAP_ONE_YAPA_API_ENDPOINT_URL = "http://" + TAP_SERVER + "/mobile/1/payloads/";

    public final static String TAP_TXN_API_ENDPOINT_URL = "http://" + TAP_SERVER + "/mobile/1/transactions.json";


    //public final static String TAP_USER_TXNS_API_ENDPOINT_URL = "http://192.168.1.135:3000/mobile/1/transactions.json";


//s3
    public final static String MY_ACCESS_KEY_ID = "AKIAJOXBJKXXTLB2MXXQ";
    public final static String MY_SECRET_KEY = "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";
    public final static String TAP_S3_BUCK = "tapyapa";
/*/// live mode

    private final static String TAP_REGISTER_API_ENDPOINT_URL = "http://192.168.1.132/mobile/1/registrations.json";
    private final static String TAP_TAGS_API_ENDPOINT_URL = "http://192.168.1.132/mobile/1/nfc_tags.json";
    private final static String TAP_USER_API_ENDPOINT_URL = "http://192.168.1.132/mobile/1/users/me";
    private final static String TAP_USERNICK_API_ENDPOINT_URL = "http://192.168.1.132/mobile/1/users/reset_nickname";
//*/

    private static String mAuthToken;
    public static String getAuthToken(){
        return mAuthToken;
    }
    public static void setAuthToken(String new_value){
        mAuthToken = new_value;
    }
    private static TapUser mTapUser;
    public static TapUser getTapUser(Context context){
        if (mTapUser == null){
            mTapUser = new TapUser();
        }
        return mTapUser;
    }
    private static AmazonS3Client mS3Client;
    private static AmazonS3Client myS3()
    {
        if (mS3Client == null){
            mS3Client = new AmazonS3Client( new BasicAWSCredentials( TapCloud.MY_ACCESS_KEY_ID, TapCloud.MY_SECRET_KEY ) );
        }
        return mS3Client;
    }
    public JSONObject httpPut(String url, JSONObject json){
        //TODO: Create this one time in class instantiation vs. here and destory later
        client = new DefaultHttpClient();
        //END

        HttpPut post = new HttpPut(url);
        String response = null;
        JSONObject output = new JSONObject();
        try {
            try {
                StringEntity se = new StringEntity(json.toString());
                post.setEntity(se);

                // setup the request headers
                post.setHeader("Accept", "application/json");
                post.setHeader("Content-Type", "application/json");

                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                response = client.execute(post, responseHandler);
                output = new JSONObject(response);

            } catch (HttpResponseException e) {
                e.printStackTrace();
                Log.e("ClientProtocol", "" + e);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("IO", "" + e);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }


        return output;
    }


    public JSONObject httpPost(String url, JSONObject json){
        //TODO: Create this one time in class instantiation vs. here and destory later
        client = new DefaultHttpClient();
        //END

        HttpPost post = new HttpPost(url);
        String response = null;
        JSONObject output = new JSONObject();
        try {
            try {

                // setup the request headers
                post.setHeader("Accept", "application/json");
                post.setHeader("Content-Type", "application/json");

                StringEntity se = new StringEntity(json.toString());
                post.setEntity(se);


                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                response = client.execute(post, responseHandler);
                output = new JSONObject(response);


            } catch (HttpResponseException e) {
                e.printStackTrace();
                Log.e("ClientProtocol", "" + e);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("IO", "" + e);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }


        return output;
    }


    public JSONObject httpGet(String url) {
        //URLEncoder.encode
        StringBuilder builder = new StringBuilder();
        //TODO: Create this one time in class instantiation vs. here and destory later
        client = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(url);
        String response = null;
        JSONObject output = new JSONObject();

        try {
            try {
                ResponseHandler<String> responseHandler = new BasicResponseHandler();
                response = client.execute(httpGet, responseHandler);
                output = new JSONObject(response);

            } catch (HttpResponseException e) {
                e.printStackTrace();
                Log.e("ClientProtocol", "" + e);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("IO", "" + e);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSON", "" + e);
        }


        return output;
    }


    public String uploadToS3withURI(Uri mURI, String s3_key, Context mContext){
        //String MY_ACCESS_KEY_ID = "AKIAJOXBJKXXTLB2MXXQ";
        //String MY_SECRET_KEY = "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";
        AmazonS3Client s3Client = myS3();
                //new AmazonS3Client( new BasicAWSCredentials( TapCloud.MY_ACCESS_KEY_ID, TapCloud.MY_SECRET_KEY ) );
        //Bucket b =  s3Client.createBucket( "test" );
        String c = getRealPathFromURI(mContext, mURI);

        PutObjectRequest por = new PutObjectRequest( TapCloud.TAP_S3_BUCK, s3_key   ,  new File(c) );
        s3Client.putObject( por );

        ResponseHeaderOverrides override = new ResponseHeaderOverrides();
        override.setContentType("image/jpeg");
        return s3Client.getResourceUrl(TapCloud.TAP_S3_BUCK, s3_key);

    }

    public String uploadToS3withStream(byte[] thumb, String s3_key, Context mContext){
        //String MY_ACCESS_KEY_ID = "AKIAJOXBJKXXTLB2MXXQ";
        //String MY_SECRET_KEY = "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";
        AmazonS3Client s3Client = myS3();
                //new AmazonS3Client( new BasicAWSCredentials( TapCloud.MY_ACCESS_KEY_ID, TapCloud.MY_SECRET_KEY ) );
        String mURL = "";


        try {
            Long contentLength = Long.valueOf(thumb.length) ;
//            Bitmap thumbnail = BitmapFactory.decodeByteArray(thumb, 0, thumb.length);
//            ByteArrayOutputStream bos = new ByteArrayOutputStream();
//            thumbnail.compress(Bitmap.CompressFormat.PNG, 0 /*ignored for PNG*/, bos);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(thumb);

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(contentLength);

            PutObjectRequest por = new PutObjectRequest( TapCloud.TAP_S3_BUCK, s3_key, inputStream, metadata );
            s3Client.putObject( por );
            ResponseHeaderOverrides override = new ResponseHeaderOverrides();
            override.setContentType("image/jpeg");
            mURL= s3Client.getResourceUrl(TapCloud.TAP_S3_BUCK, s3_key);
        }
        catch (Exception e){
            Log.e ("error", e.toString());
        }


        return mURL;




    }

    public static String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }




    public static Drawable LoadImageFromWebOperations(String url) {
        try {
            InputStream is = (InputStream) new URL(url).getContent();
            Drawable d = Drawable.createFromStream(is, "tap cloud");
            return d;
        } catch (Exception e) {
            return null;
        }
    }
    public boolean isNetworkAvailable(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager)
                mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }



    public static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        CircleImageView cmImage;
        boolean useCirle = false;

        public DownloadImageTask (CircleImageView bmImage){
            useCirle = true;
            this.cmImage = bmImage;
        }
        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            if(urldisplay != null) {
                try {
                    InputStream in = new java.net.URL(urldisplay).openStream();
                    mIcon11 = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.toString());
                    e.printStackTrace();
                }
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            if (useCirle){
                cmImage.setImageBitmap(result);
            }else{
                bmImage.setImageBitmap(result);
            }

        }
    }
}
