/**
 * Async task to redeem contact the server to redeem a voucher
 */

package co.tapdatapp.tapandroid.voucher;

import android.os.AsyncTask;
import android.os.Bundle;

import org.json.JSONObject;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.VoucherCodec;
import co.tapdatapp.tapandroid.user.Account;

public class RedeemVoucherTask extends AsyncTask<Object, Void, Void> {

    public interface Callback {
        void onComplete(VoucherRedeemResponse response);
    }

    private boolean success;
    private Throwable exception;
    private Callback callback;
    private VoucherRedeemResponse response;

    @Override
    protected Void doInBackground(Object... params) {
        if (params.length != 2) {
            throw new AssertionError(
                "Must provide callback and voucher code as execute() parameters"
            );
        }
        callback = (Callback)params[0];
        String voucher = (String)params[1];
        HttpHelper http = new HttpHelper();
        try {
            JSONObject httpResponse = http.HttpPutJSON(
                getRedeemVoucherURL(http, voucher),
                new Bundle(),
                    new JSONObject()

            );
            VoucherCodec codec = new VoucherCodec();
            response = codec.parseRedeemResponse(httpResponse);
            // Post redeeming the voucher, expire the cache of balances
            // and ensure all currency details are local.
            new Account().expireBalances();
            CurrencyDAO balance = new CurrencyDAO();
            balance.getAllBalances();
            balance.ensureLocalCurrencyDetails(response.getCurrencyId());
            success = true;
        }
        catch (Throwable e) {
            exception = e;
            success = false;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void value) {
        if (success) {
            callback.onComplete(response);
        }
        else {
            TapApplication.handleFailures(exception);
        }
    }

    /**
     * Since this is not a static URL (it changes depending on the
     * voucher code) we can't use HttpHelper to build it.
     *
     * Some code here is duplicated against HttpHelper.getFullUrl()
     * which I'm not happy with, but the pattern for URLs is very
     * consistent except for this exception
     *
     * @param http HttpHelper to get the major part of the URL
     * @param voucherCode Voucher code to redeem
     * @return A string of the complete URL
     */
    private String
    getRedeemVoucherURL(HttpHelper http, String voucherCode) {
        StringBuilder sb = new StringBuilder();
        sb.append(TapApplication.string(R.string.SERVER));
        sb.append(TapApplication.string(R.string.API_VERSION));
        sb.append(TapApplication.string(R.string.ENDPOINT_REDEEM_VOUCHER));
        sb.append("/");
        sb.append(voucherCode);
        sb.append("/redeem_voucher");
        http.appendAuthTokenIfExists(sb);
        return sb.toString();
    }
}
