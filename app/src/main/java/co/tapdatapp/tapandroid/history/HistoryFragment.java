/**
 * Fragment that displays the transaction history, also provides links
 * to rewards
 */

package co.tapdatapp.tapandroid.history;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.GridView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.helpers.CustomViewPager;
import co.tapdatapp.tapandroid.localdata.Transaction;
import co.tapdatapp.tapandroid.yapa.YapaDisplay;

public class HistoryFragment extends Fragment implements HistorySyncCallback {

    private Activity parentActivity;
    private ProgressBar progressBar;
    private GridView gridView;
    private boolean loaded = false;

    //Animation variables
    private float touchX = 0;
    private float touchY = 0;
    private float halfY;
    private float thirtyX;
    private float sixtyX;
    private int rowNum;
    private int columnNum;

    public HistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        Display display = parentActivity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        halfY = width/2;
        thirtyX = height/3;
        sixtyX = thirtyX*2;

        return view;
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
        CustomViewPager cvp = (CustomViewPager) getActivity().findViewById(R.id.pager);
        cvp.setPagingEnabled(true);
        progressBar =
            (ProgressBar)parentActivity.findViewById(R.id.history_grid_progress_bar);
        gridView =
            (GridView)parentActivity.findViewById(R.id.history_grid_view);

        /**
         * This is to calculate the location of the grid item that was clicked to determine which
         * animation to use
         */
        gridView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchY = event.getY();
                    touchX = event.getX();

                    if (touchY <= halfY) {
                        columnNum = 1;
                    } else {
                        columnNum = 2;
                    }

                    if (touchX <= thirtyX) {
                        rowNum = 1;
                    } else if (touchX >= sixtyX) {
                        rowNum = 3;
                    } else {
                        rowNum = 2;
                    }
                }
                return false;
            }
        });

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Transaction transaction = new Transaction();
                transaction.moveToByOrder(position);
                Intent openYapa = new Intent(
                    getActivity(),
                    new YapaDisplay().getDisplayClass(transaction)
                );
                openYapa.putExtra(
                        YapaDisplay.TRANSACTION_ID,
                        transaction.getSlug()
                );
                startActivity(openYapa);

                if(rowNum == 1 && columnNum == 1){
                    parentActivity.overridePendingTransition(R.anim.grid_zoom_1x1,R.anim.fade_out);
                }
                else if(rowNum == 2 && columnNum ==1){
                    parentActivity.overridePendingTransition(R.anim.grid_zoom_2x1,R.anim.fade_out);
                }
                else if(rowNum == 3 && columnNum ==1){
                    parentActivity.overridePendingTransition(R.anim.grid_zoom_3x1,R.anim.fade_out);
                }
                else if(rowNum == 1 && columnNum ==2){
                    parentActivity.overridePendingTransition(R.anim.grid_zoom_1x2,R.anim.fade_out);
                }
                else if(rowNum == 2 && columnNum ==2){
                    parentActivity.overridePendingTransition(R.anim.grid_zoom_2x2,R.anim.fade_out);
                }
                else {
                    parentActivity.overridePendingTransition(R.anim.grid_zoom_3x2,R.anim.fade_out);
                }

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
}
