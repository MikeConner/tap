/**
 * Activity for updating tag name, managing rewards, and calling the
 * Activity to write a tag.
 */

package co.tapdatapp.tapandroid.tags;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import java.util.UUID;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Tag;
import co.tapdatapp.tapandroid.localdata.Yapa;

public class ManageTagActivity
extends Activity
implements YapaAdapter.OnChangeListener,
           TextWatcher {

    public final static String MODE = "mode";
    public final static String TAG_ID = "tagId";
    public final static int MODE_NEW = 1;
    public final static int MODE_MODIFY = 2;

    private final static String NEW_TAG = "=NEW=TAG=";

    private boolean needsSaved;
    private Tag tag = null;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_manage_tag);
        Intent intent = getIntent();
        int mode = intent.getIntExtra(MODE, 0);
        needsSaved = mode == MODE_NEW;
        tag = new Tag();
        if (mode == MODE_NEW) {
            tag.remove(NEW_TAG);
            tag.create(NEW_TAG, "New Tag", new Yapa[0]);
            tag.moveTo(NEW_TAG);
        }
        else {
            String tagId = intent.getStringExtra(TAG_ID);
            tag.moveTo(tagId);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setActionButtonState();
        fillIn();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        tag.remove(NEW_TAG);
    }

    /**
     * Fill in the page if an existing tag was provided
     */
    private void fillIn() {
        EditText tagName = (EditText)findViewById(R.id.etTagName);
        if (tag.getTagId() != null) {
            tagName.setText(tag.getName());
            ListView yapaList = (ListView)findViewById(R.id.listYapa);
            YapaAdapter adapter = new YapaAdapter(this, tag.getTagId());
            yapaList.setAdapter(adapter);
        }
        tagName.addTextChangedListener(this);
    }

    /**
     * Enable/disable the write button as appropriate
     */
    private void setActionButtonState() {
        findViewById(R.id.btnWriteTag).setEnabled(!needsSaved);
    }

    /**
     * Called any time data is changed by the user. Sets the
     * modified flag to true and updates any interface widgets as
     * needed.
     */
    @Override
    public void onChange() {
        needsSaved = true;
        setActionButtonState();
    }

    private CharSequence tempName;

    /**
     * Required by TextWatcher
     */
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        tempName = s;
    }

    /**
     * Required by TextWatcher
     */
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!s.equals(tempName)) {
            onChange();
        }
    }

    /**
     * When text changes, update the modified status
     */
    @Override
    public void afterTextChanged(Editable s) {
        if (!s.equals(tempName)) {
            onChange();
        }
    }

    /**
     * Called when the add yapa button is clicked
     *
     * @param v The add yapa button
     */
    public void onClickAddYapa(View v) {
        Yapa y = new Yapa();
        y.setContent("");
        y.setImage("");
        y.setThreshold(tag.getNextAvailableThreshold());
        y.setThumb("");
        y.setTagId(tag.getTagId());
        y.setSlug(UUID.randomUUID());
        y.create();
        fillIn();
    }
}
