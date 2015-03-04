/**
 * Translate Yapa (also called "payload") between different formats
 */

package co.tapdatapp.tapandroid.remotedata;

import org.json.JSONException;
import org.json.JSONObject;

public class YapaCodec {

    private String tagId;
    private String content;
    private int threshold;
    private String image;
    private String thumb;
    private String slug;

    public void parse(JSONObject payload) {
        try {
            tagId = payload.getString("nfc_tag_id");
        }
        catch (JSONException je) {
            throw new AssertionError(je);
        }
    }
}
