/**
 * Fragment that displays the transaction history, also provides links
 * to rewards
 */

package co.tapdatapp.tapandroid.history;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class HistoryFragment extends Fragment {

    private Activity parentActivity;
    private ProgressBar progressBar;
    private ListView listView;
    private boolean loaded = false;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    /**
     * Save a reference to the Activity this Fragment is attached to,
     * since it's needed for its Context
     *
     * @param a Activity this Fragment is attached to
     */
    @Override
    public void onAttach(Activity a) {
        super.onAttach(a);
        parentActivity = a;
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
    }

    /**
     * Called whenever screen visibility changes. Used to determine
     * if the user has moved to this Fragment from another, and trigger
     * synchronization of the history records
     *
     * @param isVisibleToUser whether visible or not
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            if (!loaded) {
                fillInList();
            }
        }
        else {
            if (progressBar != null) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
            }
            if (listView != null) {
                listView.setVisibility(ListView.GONE);
                listView.setAdapter(null);
            }
            loaded = false;
        }
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
