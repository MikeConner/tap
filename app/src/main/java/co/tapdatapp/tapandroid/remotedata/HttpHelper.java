/**
 * Logic common to all HTTP/HTTPS requests
 */

package co.tapdatapp.tapandroid.remotedata;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.user.Account;

public class HttpHelper {

    /**
     * General a full URL from the passed in information, including an
     * auth token parameter if the account has one.
     *
     * @param endpoint The resource ID of the URL
     * @param resource The actual resource part of the URL
     * @param params Any query parameters to add to the URL
     * @return String of the URL properly formed
     */
    public String
    getFullUrl(int endpoint, String resource, Map<String, String> params) {
        StringBuilder sb = new StringBuilder();
        sb.append(TapApplication.string(R.string.SERVER));
        sb.append(TapApplication.string(R.string.API_VERSION));
        sb.append(TapApplication.string(endpoint));
        sb.append(resource);
        boolean first = true;
        for (String key : params.keySet()) {
            if (first) {
                sb.append("?");
                first = false;
            }
            else {
                sb.append("&");
            }
            sb.append(key);
            sb.append("=");
            try {
                sb.append(URLEncoder.encode(params.get(key), "US-ASCII"));
            }
            catch (UnsupportedEncodingException uee) {
                // This should never happen
                throw new AssertionError(uee);
            }
        }
        appendAuthTokenIfExists(sb, !first);
        return sb.toString();
    }

    /**
     * Generate the full URL from a resource ID pointing to the
     * specific resource. Adds in the server and API version and
     * adds the auth token if an account exists.
     *
     * @param endpointId ID of a string resource to the endpoing
     * @return full URL to the resource
     */
    public String getFullUrl(int endpointId) {
        StringBuilder sb = new StringBuilder();
        sb.append(TapApplication.string(R.string.SERVER));
        sb.append(TapApplication.string(R.string.API_VERSION));
        sb.append(TapApplication.string(endpointId));
        appendAuthTokenIfExists(sb);
        return sb.toString();
    }

    /**
     * Append an auth token to the URL if the config has one. Always
     * assumes that this is the only parameter.
     *
     * @param sb StringBuilder to append to
     */
    public void appendAuthTokenIfExists(StringBuilder sb) {
        appendAuthTokenIfExists(sb, false);
    }

    /**
     * append an auth token to the URL if the config has one, taking
     * into account whether other parameters have already been added
     *
     * @param sb StringBuilder to append to
     * @param additional true if there are already other parameters
     */
    public void
    appendAuthTokenIfExists(StringBuilder sb, boolean additional) {
        Account a = new Account();
        if (a.created()) {
            if (additional) {
                sb.append("&");
            }
            else {
                sb.append("?");
            }
            sb.append("auth_token=");
            sb.append(a.getAuthToken());
        }
    }

    /**
     * Returns a JSONObject extracted from the passed URL.
     *
     * @param url The full URL to the web resource
     * @param headers HTTP headers, Accept header is not needed
     * @return JSONObject of the returned data
     * @throws WebServiceError If the requested resulted in an error
     */
    public JSONObject
    HttpGetJSON(String url, Bundle headers) throws WebServiceError {
        headers.putString("Accept", "application/json");
        try {
            WebResponse response = HttpGet(url, headers);
            if (response.isOK()) {
                return response.getJSON();
            } else {
                throw new WebServiceError(response);
            }
        }
        catch (IOException | JSONException ioe) {
            throw new WebServiceError(ioe);
        }
    }

    /**
     * Post JSON via HTTP and return the result
     *
     * @param url full URL
     * @param headers Content-Type and Accept headers are added
     * @param payload The JSON object to post
     * @return WebResponse of the response
     * @throws WebServiceError on various network problems
     */
    public JSONObject
    HttpPostJSON(String url, Bundle headers, JSONObject payload)
    throws WebServiceError {
        headers.putString("Accept", "application/json");
        headers.putString("Content-Type", "application/json");
        try {
            WebResponse response = HttpPost(url, headers, payload.toString());
            if (response.isOK()) {
                return response.getJSON();
            } else {
                throw new WebServiceError(response);
            }
        }
        catch (IOException | JSONException ioe) {
            throw new WebServiceError(ioe);
        }
    }


    /**
     * Put  JSON via HTTP and return the result
     *
     * @param url full URL
     * @param headers Content-Type and Accept headers are added
     * @param payload The JSON object to post
     * @return WebResponse of the response
     * @throws WebServiceError on various network problems
     */
    public JSONObject
    HttpPutJSON(String url, Bundle headers, JSONObject payload)
    throws WebServiceError {
        headers.putString("Accept", "application/json");
        headers.putString("Content-Type", "application/json");
        try {
            WebResponse response = HttpPut(url, headers, payload.toString());
            if (response.isOK()) {
                return response.getJSON();
            } else {
                throw new WebServiceError(response);
            }
        }
        catch (IOException | JSONException ioe) {
            throw new WebServiceError(ioe);
        }
    }

    /**
     * Do an HTTP GET
     *
     * @param url The full URL to the web resource
     * @param headers No headers are added by this method
     * @return WebResponse containing the response
     * @throws IOException Various network errors will cause this
     */
    public WebResponse HttpGet(String url, Bundle headers)
        throws IOException {
        isNetworkAvailable();
        HttpClient webClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            get.setHeader(key, headers.getString(key));
        }
        HttpResponse response = webClient.execute(get);
        return new WebResponse(response);
    }

    /**
     * Do an HTTP POST
     *
     * @param url The full URL to post to
     * @param headers No headers are added by this method
     * @param payload The post data
     * @return WebResponse containing the response
     * @throws IOException as a result of various network errors
     */
    public WebResponse HttpPost(String url,
                                Bundle headers,
                                String payload
    ) throws IOException {
        isNetworkAvailable();
        HttpClient webClient = new DefaultHttpClient();
        HttpPost post = new HttpPost(url);
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            post.setHeader(key, headers.getString(key));
        }
        post.setEntity(new StringEntity(payload));
        HttpResponse response = webClient.execute(post);
        return new WebResponse(response);
    }

    /**
     * Do an HTTP put when the payload is a string
     */
    public WebResponse HttpPut(String url,
                               Bundle headers,
                               String payload
    ) throws IOException {
        isNetworkAvailable();
        HttpClient webClient = new DefaultHttpClient();
        HttpPut put = new HttpPut(url);
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            put.setHeader(key, headers.getString(key));
        }
        put.setEntity(new StringEntity(payload));
        HttpResponse response = webClient.execute(put);
        return new WebResponse(response);
    }

    /**
     * Do an HTTP put when the payload is a byte array
     */
    public WebResponse HttpPut(String url,
                               Bundle headers,
                               byte[] payload
    ) throws IOException {
        isNetworkAvailable();
        HttpClient webClient = new DefaultHttpClient();
        HttpPut put = new HttpPut(url);
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            put.setHeader(key, headers.getString(key));
        }
        put.setEntity(new ByteArrayEntity(payload));
        HttpResponse response = webClient.execute(put);
        return new WebResponse(response);
    }

    /**
     * Tests for network access, throws NoNetworkError if there is none
     */
    private void isNetworkAvailable() {
        ConnectivityManager cm =
            (ConnectivityManager)TapApplication.get().getSystemService(
                Context.CONNECTIVITY_SERVICE
            );
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected()) {
            throw new NoNetworkError();
        }
        HttpClient webClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(getFullUrl(R.string.ENDPOINT_PING));
        WebResponse wr;
        try {
            HttpResponse response = webClient.execute(get);
            wr = new WebResponse(response);
            if (!wr.isOK()) {
                throw new NoNetworkError("Status code not 200");
            }
        }
        catch (IOException ioe) {
            NoNetworkError nne = new NoNetworkError();
            nne.initCause(ioe);
            throw nne;
        }
        try {
            String value = wr.getJSON().getString("response");
            if (!"Pong".equals(value)) {
                throw new NoNetworkError("Incorrect response: " + value);
            }
        }
        catch (JSONException je) {
            NoNetworkError nne = new NoNetworkError();
            nne.initCause(je);
            throw nne;
        }
    }

}
