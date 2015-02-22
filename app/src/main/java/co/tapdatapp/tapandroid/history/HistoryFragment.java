/**
 * Fragment that displays the transaction history, also provides links
 * to rewards
 */

package co.tapdatapp.tapandroid.history;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.GridView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.localdata.Transaction;
import co.tapdatapp.tapandroid.yapa.YapaImage;
import co.tapdatapp.tapandroid.yapa.YapaUrl;
import co.tapdatapp.tapandroid.yapa.YapaText;

public class HistoryFragment extends Fragment implements HistorySyncCallback {

    private Activity parentActivity;
    private ProgressBar progressBar;
    private GridView gridView;
    private boolean loaded = false;
    /**
     * Defaulting yapaType to image for short-term functionality
     */
    public String yapaType = "image";

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
            (ProgressBar)parentActivity.findViewById(R.id.history_grid_progress_bar);
        gridView =
            (GridView)parentActivity.findViewById(R.id.history_grid_view);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                /**
                 * openYapa is currently set to null so that, in the future, different things will
                 * happen depending on the type of yapa. Also currently operating under the assumption that
                 * Yapas currently can only be images. More cases will be added later.
                 */
                Intent openYapa = null;

                switch (yapaType) {

                    case "image":
                        openYapa = new Intent(getActivity(), YapaImage.class);
                        break;
                    case "url":
                        openYapa = new Intent(getActivity(), YapaUrl.class);
                        break;
                    case "text":
                        openYapa = new Intent(getActivity(), YapaText.class);
                        break;
                    default:
                        throw new AssertionError("Invalid Yapa Type: " + yapaType);
                }
                startActivity(openYapa);
            }

        });
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
            if (gridView != null) {
                gridView.setVisibility(GridView.GONE);
                gridView.setAdapter(null);
            }
            loaded = false;
        }
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
        HistoryAdapter adapter = new HistoryAdapter(
            new Transaction(),
            parentActivity
        );
        gridView.setAdapter(adapter);
        loaded = true;
    }

    /**
     * Called when the background sync job fails to do its work.
     *
     * @TODO make this a user-friendly error
     */
    public void syncFailure(Throwable cause) {
        TapApplication.unknownFailure(cause);
    }

}
