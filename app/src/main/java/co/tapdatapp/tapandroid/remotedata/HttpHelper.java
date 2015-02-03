/**
 * Logic common to all HTTP/HTTPS requests
 */

package co.tapdatapp.tapandroid.remotedata;

import android.os.Bundle;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Set;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.user.Account;

public class HttpHelper {

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
        Account a = new Account();
        if (a.created()) {
            sb.append("?auth_token=");
            sb.append(a.getAuthToken());
        }
        return sb.toString();
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
        catch (Exception e) {
            throw new WebServiceError(e);
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
        HttpClient webClient = new DefaultHttpClient();
        HttpGet get = new HttpGet(url);
        Set<String> keys = headers.keySet();
        for (String key : keys) {
            get.setHeader(key, headers.getString(key));
        }
        HttpResponse response = webClient.execute(get);
        return new WebResponse(response);
    }

}
