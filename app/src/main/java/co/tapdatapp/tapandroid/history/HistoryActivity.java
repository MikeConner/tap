/**
 * Fragment that displays the transaction history, also provides links
 * to rewards
 */

package co.tapdatapp.tapandroid.history;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class HistoryActivity extends Activity {

    private Activity parentActivity;
    private ProgressBar progressBar;
    private ListView listView;
    private boolean loaded = false;

    public HistoryActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_history);
    }

    /**
     * Pull references to the widgets that will be needed later
     */
    @Override
    public void onResume() {
        super.onResume();
        progressBar =
            (ProgressBar)parentActivity.findViewById(R.id.history_progress_bar);
        listView =
            (ListView)parentActivity.findViewById(R.id.history_list_view);

        if (!loaded) {
            fillInList();
        }
    }

    /**
     * Normal onPause function
     */
    public void onPause(){
        super.onPause();

        if (progressBar != null) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }
        if (listView != null) {
            listView.setVisibility(ListView.GONE);
            listView.setAdapter(null);
        }
        loaded = false;
    }

    /**
     * Set the display to the progress spinner and kick off the
     * background job to synchronize data
     */
    private void fillInList() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        listView.setVisibility(ListView.GONE);
        new HistorySyncTask().execute(this);
    }

    /**
     * Called when HistorySync is complete to display the ListView
     * of history records
     */
    public void postSyncDisplay() {
        progressBar.setVisibility(ProgressBar.GONE);
        listView.setVisibility(ListView.VISIBLE);
        HistoryAdapter adapter = new HistoryAdapter(
            new Transaction(),
            parentActivity
        );
        listView.setAdapter(adapter);
        loaded = true;
    }

    /**
     * Called when the background sync job fails to do its work.
     *
     * @TODO make this a user-friendly error
     */
    public void syncFailure(Exception cause) {
        AssertionError e = new AssertionError("Failed to sync: see @TODO");
        e.initCause(cause);
        throw e;
    }

}
