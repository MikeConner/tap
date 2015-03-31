/**
 * Accepts a JSON object and turns it into a Transaction object.
 */

package co.tapdatapp.tapandroid.remotedata;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.ParseException;

import co.tapdatapp.tapandroid.helpers.ISO8601Format;
import co.tapdatapp.tapandroid.localdata.BaseCodec;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class TransactionCodec extends BaseCodec {

    private final ISO8601Format format = new ISO8601Format();

    /**
     * Turn the response to a get transaction web request into a
     * Transaction object
     *
     * @param input the payload of a web request
     * @return Transaciton object
     * @throws JSONException in various situations
     */
    public Transaction unmarshall(String input)
    throws JSONException, ParseException {
        return unmarshall(new JSONObject(input));
    }

    public Transaction unmarshall(JSONObject input)
    throws JSONException, ParseException {
        Transaction rv = new Transaction();
        rv.setSlug(input.getString("id"));
        // This is ridiculous. If ever there was a lousier set of
        // classes for handling date/time, I haven't seen it.
        rv.setContentType(input.getString("payload_content_type"));
        rv.setTimestamp(new Timestamp(format.parse(input.getString("date")).getTime()));
        if(rv.getContentType()=="image" || rv.getContentType() == "coupon") {
            rv.setYapa_url(ifNull(input.getString("payload_image"), null));
        }
        else {
            rv.setYapa_url(ifNull(input.getString("uri"),null));
        }
        rv.setYapa_thumb_url(ifNull(input.getString("payload_thumb"), null));
        rv.setAmount(input.getInt("amount"));
        rv.setDescription(ifNull(input.getString("description"), null));
        rv.setThumb_url(ifNull(input.getString("other_user_thumb"), null));
        rv.setNickname(ifNull(input.getString("other_user_nickname"), null));
        return rv;
    }

}
