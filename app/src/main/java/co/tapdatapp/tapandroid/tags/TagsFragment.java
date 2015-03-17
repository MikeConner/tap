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

import java.util.UUID;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.UserFriendlyError;
import co.tapdatapp.tapandroid.localdata.Tag;
import co.tapdatapp.tapandroid.localdata.Yapa;

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
        // @TODO this is generating test data, replace with real data
        generateTestTags();
        //new SyncTagsTask().execute(this);
    }

    /**
     * Generate dummy data for testing ... replace this as soon as the
     * network stuff is sorted out
     */
    // @TODO remove this code when the rest is finished
    private void generateTestTags() {
        Tag t = new Tag();
        t.removeAll();
        for (int i = 0; i < 10; i++) {
            Yapa yapa = new Yapa();
            yapa.setTagId(ManageTagActivity.NEW_TAG);
            yapa.setThreshold(1);
            yapa.setSlug(UUID.randomUUID());
            yapa.setType(Yapa.TYPE_TEXT);
            t.create("ABC-DEF-000" + i, "Tag #" + i, new Yapa[]{yapa});
        }
        onTagsSynced();
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
        try{
            throw t;
        }
        catch(UserFriendlyError ufe){
            TapApplication.errorToUser(ufe);
        }
        catch(Throwable catchall) {
            TapApplication.unknownFailure(t);
        }
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
