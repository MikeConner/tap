/**
 * Logic common to all HTTP/HTTPS requests
 */

package co.tapdatapp.tapandroid.remotedata;

import android.content.SharedPreferences;
import android.os.Bundle;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.user.Account;

public class HttpHelper {

    public String getFullUrl(int endpointId) {
        StringBuilder sb = new StringBuilder();
        sb.append(TapApplication.string(R.string.SERVER));
        sb.append(TapApplication.string(R.string.API_VERSION));
        sb.append(TapApplication.string(endpointId));
        Account a = new Account();
        if (a.created()) {
            sb.append("?auth_token=");
            sb.append(a.getAuthToken());
        }
        return sb.toString();
    }

    public JSONObject
    HttpGetJSON(String url, Bundle headers)
    throws IOException, HttpException, JSONException {
        headers.putString("Accept", "application/json");
        return new JSONObject(HttpGet(url, headers));
    }

    public String HttpGet(String url, Bundle headers)
    throws IOException, HttpException {
        HttpClient webClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            get.setHeader(key, headers.getString(key));
        }
        StringBuilder responseText = new StringBuilder();
        InputStream is = null;
        try {
            HttpResponse response = webClient.execute(get);
            StatusLine status = response.getStatusLine();
            if (status.getStatusCode() == 200) {
                HttpEntity entity = response.getEntity();
                is = entity.getContent();
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is)
                );
                String line;
                while ((line = reader.readLine()) != null) {
                    responseText.append(line);
                }
            }
            else {
                throw new HttpException(
                    "URL " + url + " returns HTTP status code " + status.getStatusCode()
                );
            }
        }
        finally {
            if (is != null) {
                is.close();
            }
        }
        return responseText.toString();
    }

}
