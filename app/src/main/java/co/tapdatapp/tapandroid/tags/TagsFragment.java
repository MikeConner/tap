package co.tapdatapp.tapandroid.tags;

import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
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

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResume() {
        super.onResume();
        getView().findViewById(R.id.btnNewTag).setOnClickListener(this);
        ((ListView)getView().findViewById(R.id.listViewTags)).setOnItemClickListener(this);
        new SyncTagsTask().execute(this);
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

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onTagsSynced() {
        TagListAdapter adapter = new TagListAdapter();
        ListView tagList = (ListView)getView().findViewById(R.id.listViewTags);
        tagList.setAdapter(adapter);
    }

    /**
     * Called when the tag sync fails
     * @param t The cause of the failure
     */
    @Override
    public void onTagSyncFailed(Throwable t) {
        TapApplication.handleFailures(t);
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
