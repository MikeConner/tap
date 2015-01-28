/**
 * Background thread to synchronize local transaction history with
 * server.
 */

package co.tapdatapp.tapandroid.history;

import android.os.AsyncTask;

import java.sql.Timestamp;
import java.util.UUID;

import co.tapdatapp.tapandroid.localdata.Transaction;

public class HistorySyncTask
extends AsyncTask<HistoryFragment, Void, Void> {

    private HistoryFragment historyFragment;

    @Override
    protected Void doInBackground(HistoryFragment... historyFragments) {
        if (historyFragments.length != 1) {
            throw new AssertionError(
                "Must be called with single HistoryFragment"
            );
        }
        historyFragment = historyFragments[0];
        // This is a hack to provide something visible to look at until
        // We have actual network data to work with
        for (int i = 0; i < 15; i++) {
            createDummyRecord(i);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void x) {
        historyFragment.postSyncDisplay();
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
