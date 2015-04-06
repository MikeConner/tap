/**
 * convert tags between various representations
 */

package co.tapdatapp.tapandroid.remotedata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.tapdatapp.tapandroid.localdata.BaseCodec;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.localdata.Tag;

public class TagCodec extends BaseCodec {

    private String id;
    private String name;

    /**
     * Parse the response from creating a new tag
     *
     * @param newTagResponse JSON returned by the webservice
     */
    public void
    parseSavedTagResponse(JSONObject newTagResponse)
    throws JSONException {
        JSONObject response = newTagResponse.getJSONObject("response");
        id = response.getString("id");
        if (id == null || id.isEmpty()) {
            throw new AssertionError("Empty tag ID");
        }
        name = response.getString("name");
    }

    /**
     * Parse the response from requesting tag details
     *
     * @param response JSON from requesting tag details
     * @return A Tag object initialized from the JSON
     */
    public Tag parseGetTagResponse(JSONObject response)
    throws JSONException {
        Tag rv = new Tag();
        rv.setTagId(response.getString("id"));
        rv.setName(response.getString("name"));
        return rv;
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

    /**
     * Create JSON for creating/updating tags on the Ruby server
     *
     * @param t Tag to marshall into JSON
     * @return a JSON object suitable for the server
     * @throws JSONException if anything goes wrong
     */
    public JSONObject marshallFullTag(Tag t) throws JSONException {
        JSONObject rv = new JSONObject();
        JSONObject tag = new JSONObject();
        tag.put("name", t.getName());
        int currencyId = t.getCurrencyId();
        if (currencyId != CurrencyDAO.CURRENCY_BITCOIN) {
            tag.put("currency_id", currencyId);
        }
        String tagId = t.getTagId();
        if (!Tag.NEW_TAG_ID.equals(tagId)) {
            tag.put("tag_id", tagId);
        }
        rv.put("tag", tag);
        YapaCodec yapaCodec = new YapaCodec();
        JSONArray yapas = yapaCodec.marshallYapaArray(t.getYapa());
        rv.put("payloads", yapas);
        return rv;
    }
}
