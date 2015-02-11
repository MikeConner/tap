/**
 * Activity that displays the transaction history, also provides links
 * to rewards
 */

package co.tapdatapp.tapandroid.history;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.ProgressBar;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class HistoryActivity extends Activity implements HistorySyncCallback {

    private ProgressBar progressBar;
    private GridView gridView;
    private boolean loaded = false;

    public HistoryActivity() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
    }

    /**
     * Pull references to the widgets that will be needed later
     */
    @Override
    public void onResume() {
        super.onResume();
        progressBar = (ProgressBar)findViewById(R.id.history_grid_progress_bar);
        gridView = (GridView)findViewById(R.id.history_grid_view);

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
        if (gridView != null) {
            gridView.setVisibility(GridView.GONE);
            gridView.setAdapter(null);
        }
        loaded = false;
    }

    /**
     * Set the display to the progress spinner and kick off the
     * background job to synchronize data
     */
    public void fillInList() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        gridView.setVisibility(GridView.GONE);
        new HistorySyncTask().execute(this);
    }

    /**
     * Called when HistorySync is complete to display the ListView
     * of history records
     */
    public void postSyncDisplay() {
        progressBar.setVisibility(ProgressBar.GONE);
        gridView.setVisibility(GridView.VISIBLE);
        gridView = (GridView) findViewById(R.id.history_grid_view);
        gridView.setAdapter(new HistoryActivityAdapter(new Transaction(), this));
        loaded = true;
    }

    /**
     * Called when the background sync job fails to do its work.
     *
     * @param cause The exception that caused the failure
     */
    // @TODO make this a user-friendly error
    public void syncFailure(Exception cause) {
        AssertionError e = new AssertionError("Failed to sync: see @TODO");
        e.initCause(cause);
        throw e;
    }


}
