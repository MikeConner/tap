/**
 * Background thread to synchronize local transaction history with
 * server.
 */

package co.tapdatapp.tapandroid.history;

import android.os.AsyncTask;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.ISO8601Format;
import co.tapdatapp.tapandroid.localdata.Transaction;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.TransactionCodec;

public class HistorySyncTask
extends AsyncTask<HistorySyncCallback, Void, Void> {

    private HistorySyncCallback historyFragment;
    private boolean success = false;
    private Throwable exception;

    @Override
    protected Void doInBackground(HistorySyncCallback...historyFragments) {
        if (historyFragments.length != 1) {
            throw new AssertionError(
                "Must be called with single HistoryFragment"
            );
        }
        historyFragment = historyFragments[0];
        try {
            syncWithServer();
            success = true;
        }
        catch (Throwable t) {
            exception = t;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void x) {
        if (success) {
            historyFragment.postSyncDisplay();
        }
        else {
            TapApplication.handleFailures(exception);
        }
    }

    /**
     * Pull down any data from the server newer than what we already
     * have
     */
    public void syncWithServer() throws Exception {
        HttpHelper http = new HttpHelper();
        TransactionCodec tc = new TransactionCodec();
        Date d = new Transaction().getNewest();
        ISO8601Format df = new ISO8601Format();
        HashMap<String, String> params = new HashMap<>();
        params.put("after", df.format(d));
        String url = http.getFullUrl(
            R.string.ENDPOINT_TRANSACTION_LIST,
            "",
            params
        );
        JSONObject response = http.HttpGetJSON(url, new Bundle());
        JSONArray responses = response.getJSONArray("response");
        int responseNum = responses.length();

        for (int i = 0; i < responseNum; i++) {
            JSONObject oneResponse = responses.getJSONObject(i);
            Transaction t = tc.unmarshall(oneResponse);
            // The server may return transactions that do not contain
            // payloads. At this time, we're not recording those
            // locally
            if (t.getYapa_url() != null && !t.getYapa_url().equals("null")) {
                t.create();
            }
        }
    }

}
