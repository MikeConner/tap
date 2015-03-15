package co.tapdatapp.tapandroid.service;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.http.client.HttpResponseException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

public class TapCloud {
    DefaultHttpClient client;

    private static TapUser mTapUser;
    public static TapUser getTapUser(Context context){
        if (mTapUser == null){
            mTapUser = new TapUser();
        }
        return mTapUser;
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
                    throw new AssertionError(e);
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
