/**
 * Encapsulate an HttpResponse with some additional helper code to
 * make things more accessible.
 */

package co.tapdatapp.tapandroid.remotedata;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

public class WebResponse {

    private final int responseCode;
    private final HttpResponse httpResponse;

    public WebResponse(HttpResponse r) {
        httpResponse = r;
        responseCode = r.getStatusLine().getStatusCode();
    }

    /**
     * Return the body of the response.
     *
     * @return The bytes of the response body
     */
    public byte[] getBody() {
        HttpEntity entity = httpResponse.getEntity();
        byte[] bodyData = new byte[
            (int) entity.getContentLength()
            ];
        InputStream is = null;
        try {
            is = entity.getContent();
            int position = 0;
            int bytesRead = 0;
            while (bytesRead != -1) {
                bytesRead = is.read(
                    bodyData,
                    position,
                    bodyData.length - position
                );
                position += bytesRead;
            }
        } catch (IOException ioe) {
            bodyData = null;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException ioe) {
                    // If this happens, there really isn't anything
                    // that can be done.
                }
            }
        }
        return bodyData;
    }

    /**
     * Returns the body as a JSON object
     *
     * @return JSON object from the response body
     * @throws JSONException If the body is not valid JSON
     */
    public JSONObject getJSON() throws JSONException {
        return new JSONObject(new String(getBody()));
    }

    /**
     * Returns false if the HTTP code wasn't a standard "OK"
     *
     * @return true if the request returned an HTTP 200 code
     */
    public boolean isOK() {
        return responseCode == 200;
    }

    /**
     * @return MIME-type of the response body
     */
    public String getMediaType() {
        Header h = httpResponse.getFirstHeader("Content-Type");
        String value = h.getValue();
        if (value.contains(";")) {
            return value.split(";")[0];
        }
        else {
            return value;
        }
    }

    /**
     * Create a useful string of response if it was an error
     *
     * @return informational string if the response was an error
     */
    public String getError() {
        if (isOK()) {
            return "";
        }
        else {
            // @TODO: more detailed error information
            return Integer.toString(responseCode);
        }
    }
}
