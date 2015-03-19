/**
 * Translate Yapa (also called "payload") between different formats
 */

package co.tapdatapp.tapandroid.remotedata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.tapdatapp.tapandroid.localdata.Yapa;

public class YapaCodec {

    /**
     * Parse JSON supplied when requesting the list of tags into a
     * Yapa object.
     *
     * @param tagId Tag ID to associate the Yapa with
     * @param payload JSON for a single Yapa
     * @return Yapa object from teh provided JSON
     * @throws JSONException
     */
    public Yapa parse(String tagId, JSONObject payload) throws JSONException {
        Yapa rv = new Yapa();
        rv.setTagId(tagId);
        rv.setUri(payload.getString("uri"));
        rv.setContent(payload.getString("content"));
        rv.setThreshold(payload.getInt("threshold"));
        rv.setImage(payload.getString("payload_image"));
        rv.setThumb(payload.getString("payload_thumb"));
        rv.setDescription(payload.getString("description"));
        rv.setType(payload.getString("content_type"));
        return rv;
    }

    /**
     * Create appropriate JSON for an array of Yapa for submitting to
     * the server.
     *
     * @param list Array of Yapa
     * @return JSON of the passed Yapa
     * @throws JSONException on various problems
     */
    public JSONArray
    marshallYapaArray(Yapa[] list) throws JSONException {
        JSONArray rv = new JSONArray();
        for (Yapa item : list) {
            JSONObject y = new JSONObject();
            y.put("threshold", item.getThreshold());
            y.put("content_type", item.getType());
            y.put("description", "Desc" + item.getDescription());
            y.put("content", item.getContent());
            y.put("payload_image", item.getImage());
            y.put("payload_thumb", item.getThumb());
            y.put("uri", item.getUri());
            rv.put(y);
        }
        return rv;
    }

}
