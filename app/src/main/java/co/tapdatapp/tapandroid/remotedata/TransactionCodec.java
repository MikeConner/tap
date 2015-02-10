/**
 * Accepts a JSON object and turns it into a Transaction object.
 */

package co.tapdatapp.tapandroid.remotedata;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;

import co.tapdatapp.tapandroid.localdata.Transaction;

public class TransactionCodec {

    public Transaction unmarshall(String input)
    throws JSONException {
        return unmarshall(new JSONObject(input));
    }

    public Transaction unmarshall(JSONObject input)
    throws JSONException {
        Transaction rv = new Transaction();
        rv.setSlug(input.getString("id"));
        rv.setTimestamp(Timestamp.valueOf(input.getString("date")));
        rv.setYapa_url(input.getString("payload_image"));
        rv.setYapa_thumb_url(input.getString("payload_thumb"));
        rv.setAmount(input.getInt("amount"));
        rv.setDescription(input.getString("comment"));
        rv.setThumb_url(input.getString("other_user_thumb"));
        rv.setNickname(input.getString("other_user_nickname"));
        return rv;
    }
}
