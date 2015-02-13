/**
 * convert tags between various representations
 */

package co.tapdatapp.tapandroid.remotedata;

import org.json.JSONException;
import org.json.JSONObject;

public class TagCodec {

    private String id;
    private String name;

    /**
     * Create a tag object from the webservice
     *
     * @param newTagResponse JSON returned by the webservice
     */
    public TagCodec(JSONObject newTagResponse) {
        try {
            JSONObject response = newTagResponse.getJSONObject("response");
            id = response.getString("id");
            name = response.getString("name");
        }
        catch (JSONException je) {
            throw new AssertionError(je);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}