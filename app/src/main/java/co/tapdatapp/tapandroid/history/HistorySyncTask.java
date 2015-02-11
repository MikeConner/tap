/**
 * Background thread to synchronize local transaction history with
 * server.
 */

package co.tapdatapp.tapandroid.history;

import android.os.AsyncTask;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Timestamp;
import java.util.UUID;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Transaction;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;

public class HistorySyncTask
extends AsyncTask<HistorySyncCallback, Void, Void> {

    private HistorySyncCallback historyFragment;
    private boolean success = false;
    private Exception exception;

    @Override
    protected Void doInBackground(HistorySyncCallback...historyFragments) {
        if (historyFragments.length != 1) {
            throw new AssertionError(
                "Must be called with single HistoryFragment"
            );
        }
        historyFragment = historyFragments[0];
        /*
        // This section is intended to actually communicate with the
        // server, but it doesn't look like all the pieces are in place
        // to do that yet.
        HttpHelper http = new HttpHelper();
        try {
            JSONObject response = http.HttpGetJSON(
                http.getFullUrl(R.string.ENDPOINT_TRANSACTION_LIST),
                new Bundle()
            );
            JSONArray responses = response.getJSONArray("response");
            int responseNum = responses.length();

            for (int i = 0; i < responseNum; i++) {
                JSONObject oneResponse = responses.getJSONObject(i);

            }
            success = true;
        }
        catch (Exception e) {
            exception = e;
            success = false;
        }
        // End untested live code
        */
        // This is a hack to provide something visible to look at until
        // We have actual network data to work with
        for (int i = 0; i < 15; i++) {
            createDummyRecord(i);
        }
        success = true;
        // end testing section
        return null;
    }

    @Override
    protected void onPostExecute(Void x) {
        if (success) {
            historyFragment.postSyncDisplay();
        }
        else {
            historyFragment.syncFailure(exception);
        }
    }

    /**
     * Hack to provide example data until such time as there's real
     * data
     *
     * @param index
     */
    private void createDummyRecord(int index) {
        Transaction t = new Transaction();
        t.setAmount(5 + index);
        t.setDescription("Generated Test transaction # " + index );
        t.setSlug(UUID.randomUUID().toString());
        t.setThumb_url("http://www.example.com");
        t.setYapa_url("http://www.example.com");
        t.setTimestamp(Timestamp.valueOf("2014-11-" + Integer.toString(index + 10) + " 01:00:00.0"));
        t.setNickname("Nickname" + index);
        t.create();
    }
}
