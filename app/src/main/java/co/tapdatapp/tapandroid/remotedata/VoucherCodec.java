/**
 * For translating voucher data between different representations
 */

package co.tapdatapp.tapandroid.remotedata;

import org.json.JSONException;
import org.json.JSONObject;

import co.tapdatapp.tapandroid.voucher.VoucherRedeemResponse;

public class VoucherCodec {

    private final static String BALANCE = "balance";
    private final static String AMOUNT_REDEEMED = "amount_redeemed";
    private final static String CURRENCY = "currency";
    private final static String CURRENCY_ID = "id";

    /**
     * Parse a redemption response and return an object
     *
     * @param in The JSON of the response to a redemption request
     */
    public VoucherRedeemResponse parseRedeemResponse(JSONObject in) {
        VoucherRedeemResponse rv = new VoucherRedeemResponse();
        try {
            rv.setBalance(in.getInt(BALANCE));
            rv.setAmountRedeemed(in.getInt(AMOUNT_REDEEMED));
            rv.setCurrencyId(in.getJSONObject(CURRENCY).getInt(CURRENCY_ID));
        }
        catch (JSONException je) {
            throw new AssertionError(je);
        }
        return rv;
    }

}
