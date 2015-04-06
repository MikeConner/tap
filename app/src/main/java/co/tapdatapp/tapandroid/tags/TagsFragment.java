package co.tapdatapp.tapandroid.tags;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;

public class TagsFragment
extends Fragment
implements View.OnClickListener,
           AdapterView.OnItemClickListener,
           SyncTagsTask.Callback {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState
    ) {
        new GetMyCurrenciesTask().execute();
        return inflater.inflate(R.layout.fragment_tags, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        View v = getView();
        if (v == null) {
            Log.e("IGNORED", "onResume() getView() returned null", new Exception());
            return;
        }
        v.findViewById(R.id.btnNewTag).setOnClickListener(this);
        ((ListView)v.findViewById(R.id.listViewTags)).setOnItemClickListener(this);
        new SyncTagsTask().execute(this);
    }

    /**
     * Free the adapter when the view is paused, which both saves
     * memory and prevents problems with the adapter getting out of
     * sync with the data.
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public void onPause() {
        super.onPause();
        try {
            ((ListView)getView().findViewById(R.id.listViewTags)).setAdapter(null);
        }
        catch (NullPointerException npe) {
            // Ignore for race conditions where the view isn't
            // created yet
        }
    }

    /**
     * Free the adapter when paging away and reinstate it when
     * returning. Both saves memory and prevents errors if something
     * else causes the adapter to become out of sync.
     */
    @SuppressWarnings("ConstantConditions")
    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible) {
            onTagsSynced();
        }
        else {
            try {
                ((ListView)getView().findViewById(R.id.listViewTags)).setAdapter(null);
            }
            catch (NullPointerException npe) {
                // Ignore for race conditions where the view isn't
                // created yet
            }
        }
    }

    /**
     * Handles clicks on the "new tag" button
     *
     * @param v The view that was clicked
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnNewTag :
                Intent i = new Intent(getActivity(), SelectTagTypeActivity.class);
                startActivity(i);
                break;
            default :
                throw new AssertionError("Unknown button " + v.getId());
        }
    }

    @Override
    public void onTagsSynced() {
        try {
            @SuppressWarnings("ConstantConditions")
            ListView tagList = (ListView) getView().findViewById(R.id.listViewTags);
            TagListAdapter adapter = new TagListAdapter();
            tagList.setAdapter(adapter);
        }
        catch (NullPointerException npe) {
            // This can happen if the user navigates away from the
            // Activity faster than the background task can finish,
            // and can be ignored
        }
    }

    @Override
    public void onTagSyncError(Throwable t) {
        TapApplication.handleFailures(getActivity(), t);
    }

    /**
     * Called when a particular item in the tag list is clicked
     */
    @Override
    public void
    onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        Intent i = new Intent(getActivity(), ManageTagActivity.class);
        i.putExtra(ManageTagActivity.MODE, ManageTagActivity.MODE_MODIFY);
        i.putExtra(
            ManageTagActivity.TAG_ID,
            ((TextView)view.findViewById(R.id.TagLineItemId)).getText()
        );
        startActivity(i);
    }
}
