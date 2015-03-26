/**
 * Translate Yapa (also called "payload") between different formats
 */

package co.tapdatapp.tapandroid.remotedata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.tapdatapp.tapandroid.localdata.BaseCodec;
import co.tapdatapp.tapandroid.localdata.Yapa;

public class YapaCodec extends BaseCodec {

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
        rv.setUri(ifNull(payload.getString("uri"), null));
        rv.setContent(ifNull(payload.getString("content"), null));
        rv.setThreshold(payload.getInt("threshold"));
        rv.setImage(ifNull(payload.getString("payload_image"), null));
        rv.setThumb(ifNull(payload.getString("payload_thumb"), null));
        rv.setDescription(ifNull(payload.getString("description"), null));
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
