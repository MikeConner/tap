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
import java.util.ArrayList;

public class WebResponse {

    private final int responseCode;
    private final HttpResponse httpResponse;
    private byte[] body = null;

    public WebResponse(HttpResponse r) {
        httpResponse = r;
        responseCode = r.getStatusLine().getStatusCode();
    }

    /**
     * Return the body of the response.
     *
     * @return The bytes of the response body
     */
    @SuppressWarnings("ThrowFromFinallyBlock")
    public byte[] getBody() {
        if (body == null) {
            HttpEntity entity = httpResponse.getEntity();
            ArrayList<Byte> bytes = new ArrayList<>();
            InputStream is = null;
            try {
                is = entity.getContent();
                int b;
                while ((b = is.read()) != -1) {
                    bytes.add((byte)b);
                }
                body = new byte[bytes.size()];
                for (int i = 0; i < body.length; i++) {
                    body[i] = bytes.get(i);
                }
            } catch (IOException ioe) {
                body = null;
            } finally {
                if (is != null) try {
                    is.close();
                } catch (IOException ioe) {
                    throw new AssertionError(ioe);
                }
            }
        }
        return body;
    }

    /**
     * Returns the body as a JSON object
     *
     * @return JSON object from the response body
     * @throws JSONException If the body is not valid JSON
     */
    public JSONObject getJSON() throws JSONException {
        String body = new String(getBody());
        if (body.isEmpty()) {
            return new JSONObject();
        }
        else {
            return new JSONObject(body);
        }
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
     * @return An error safe to show the user, or null if there is none
     */
    public String getUserError() {
        try {
            return getJSON().getString("user_error");
        }
        catch (Throwable t) {
            // If anything fails while trying to get the error, just
            // return null
            return null;
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
            String message;
            try {
                JSONObject error = getJSON();
                message = error.getString("error_description");
            }
            catch (JSONException je) {
                message = new String(getBody());
            }
            catch (Exception e) {
                message = e.getMessage();
            }
            return Integer.toString(responseCode) + ": " + message;
        }
    }
}
