/**
 * Accepts a JSON object and turns it into a Transaction object.
 */

package co.tapdatapp.tapandroid.remotedata;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.text.ParseException;

import co.tapdatapp.tapandroid.helpers.ISO8601Format;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.localdata.Transaction;
import co.tapdatapp.tapandroid.user.Account;

public class TransactionCodec {

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
        rv.setTimestamp(new Timestamp(format.parse(input.getString("date")).getTime()));
        rv.setYapa_url(input.getString("payload_image"));
        rv.setYapa_thumb_url(input.getString("payload_thumb"));
        rv.setContentType(input.getString("payload_content_type"));
        rv.setAmount(input.getInt("amount"));
        rv.setDescription(input.getString("comment"));
        rv.setThumb_url(input.getString("other_user_thumb"));
        rv.setNickname(input.getString("other_user_nickname"));
        return rv;
    }

    /**
     * Create the JSON needed to send a transaction
     *
     * @param tagId The tag ID to send money to
     * @param amount The amount to send
     * @param currencyId Currency ID the request is to be sent for
     * @return JSONObject of the serialized request
     */
    public JSONObject
    marshallNewTransaction(String tagId, int amount, int currencyId) {
        JSONObject rv = new JSONObject();
        try {
            rv.put("auth_token", new Account().getAuthToken());
            rv.put("tag_id", tagId);
            rv.put("amount", Integer.toString(amount));
            if (currencyId != CurrencyDAO.CURRENCY_BITCOIN) {
                rv.put("currency_id", currencyId);
            }
        }
        catch (JSONException je) {
            throw new AssertionError(je);
        }
        return rv;
    }
}
