/**
 * Use the extended dev API to store images, video, etc
 */

package co.tapdatapp.tapandroid.remotedata;

import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;

import java.io.IOException;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;

public class DevStorage implements RemoteStorageDriver {

    private final HttpHelper httpHelper;

    private final String fetchUrl;
    private final String putUrl;

    DevStorage() {
        httpHelper = new HttpHelper();
        String baseUrl = TapApplication.string(R.string.SERVER) +
                         TapApplication.string(R.string.API_VERSION) +
                         "developer/";
        fetchUrl = baseUrl + "file/";
        putUrl = baseUrl + "store";
    }

    @Override
    public String store(byte[] data, String type)
    throws IOException, WebServiceError {
        WebResponse r = httpHelper.HttpPut(putUrl, new Bundle(), data);
        if (r.isOK()) {
            try {
                return fetchUrl + r.getJSON().getString("id");
            }
            catch (JSONException je) {
                throw new WebServiceError(je);
            }
        }
        else {
            throw new WebServiceError(r);
        }
    }

    @Override
    public void overWrite(String url, byte[] data)
    throws IOException, WebServiceError {
        String[] bits = url.split("/");
        url = bits[bits.length - 1];
        Log.d("JSON", putUrl + "/" + url);
        WebResponse r = httpHelper.HttpPut(
            putUrl + "/" + url,
            new Bundle(),
            data
        );
        if (!r.isOK()) {
            throw new WebServiceError(r);
        }
    }
}
