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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.Files;
import co.tapdatapp.tapandroid.localdata.AndroidCache;
import co.tapdatapp.tapandroid.localdata.Tag;
import co.tapdatapp.tapandroid.localdata.Yapa;

public class ManageTagActivity
extends Activity
implements TextWatcher,
           SaveTagToServerTask.Callback {

    public final static String MODE = "mode";
    public final static String TAG_ID = "tagId";
    public final static int MODE_NEW = 1;
    public final static int MODE_MODIFY = 2;

    private boolean needsSaved;
    private Tag tag = null;
    private ImageYapaLineItem imageSelectedCallback;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_manage_tag);
        Intent intent = getIntent();
        int mode = intent.getIntExtra(MODE, 0);
        if (mode == 0) {
            throw new AssertionError("must provide a mode");
        }
        needsSaved = mode == MODE_NEW;
        tag = new Tag();
        String tagId = intent.getStringExtra(TAG_ID);
        tag.moveTo(tagId);
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
        tag.remove(Tag.NEW_TAG_ID);
    }

    /**
     * Fill in the page if an existing tag was provided
     */
    private void fillIn() {
        EditText tagName = (EditText)findViewById(R.id.etTagName);
        if (tag.getTagId() != null) {
            tagName.setText(tag.getName());
            ListView yapaList = (ListView)findViewById(R.id.listYapa);
            YapaAdapter adapter = new YapaAdapter(this, tag);
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

    /**
     * Called when the SAVE button is tapped to save to the server
     */
    public void onClickSave(View view) {
        saveUI();
        new SaveTagToServerTask().execute(this, tag.getTagId());
    }

    /**
     * Take what's in the UI and save it to SQLite
     */
    private void saveUI() {
        tag.setName(((EditText)findViewById(R.id.etTagName)).getText().toString());
        tag.update();
    }

    /**
     * Callback when updating the tag on the server is complete
     *
     * @param TagId Tag ID of what was saved, will be different if a new tag was created
     */
    @Override
    public void onTagSaved(String TagId) {
        // @TODO ... um ... what do do here?
    }

    /**
     * Called if saving the tag to the server fails
     */
    @Override
    public void onTagSaveFailed(Throwable t) {
        TapApplication.handleFailures(t);
    }


    /**
     * When an external Activity is used to create or select a file
     * (such as an image or a video) this gets the result, has to
     * figure out how to handle it, and call the appropriate callback
     * to the line item that started the whole thing.
     *
     * @param requestCode constants in YapaLineItem
     * @param resultCode RESULT_OK or RESULT_CANCELLED per spec
     * @param data data based on the Activity that was called
     */
    // @TODO I'm concerned that this is going to cause trouble running
    // on the UI thread, as it's likely to be time-consuming, but it
    // might be worth avoiding making it more complicated until it's
    // proven to be necessary.
    @SuppressWarnings("ThrowFromFinallyBlock")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case YapaLineItem.SELECT_PICTURE :
                if (resultCode == RESULT_OK) {
                    InputStream imageStream;
                    try {
                        imageStream = getContentResolver().openInputStream(data.getData());
                    }
                    catch (FileNotFoundException fnfe) {
                        TapApplication.errorToUser(TapApplication.string(R.string.file_access_problem));
                        return;
                    }
                    String imageId = UUID.randomUUID().toString();
                    AndroidCache cache = new AndroidCache();
                    try {
                        cache.put(imageId, "", Files.readAllBytes(imageStream));
                    }
                    catch (IOException ioe) {
                        TapApplication.errorToUser(TapApplication.string(R.string.file_access_problem));
                        return;
                    }
                    finally {
                        if (imageStream != null) {
                            try {
                                imageStream.close();
                            }
                            catch (IOException ioe) {
                                // An exception trying to close a file
                                // opened read-only? Phone is probably
                                // broken
                                throw new AssertionError(ioe);
                            }
                        }
                    }
                    imageSelectedCallback.onImageSet(imageId, cache);
                }
                else {
                    TapApplication.errorToUser(TapApplication.string(R.string.no_image_selected));
                }
                break;
            default :
                throw new AssertionError("Unknown request code " + requestCode);
        }
    }

    /**
     * This feels clunky to me, but I can't come up with a cleaner
     * way to do it. When an image button is tapped to add a new image
     * to an image yapa, the ImageYapaLineItem object calls this,
     * passing itself. The reference to the passed object is saved as
     * imageSelectedCallback. Then, when onActivityResult is called,
     * it can send the Bitmap to the correct ImageYapaLineItem object
     * to to be displayed.
     *
     * @param i the ImageYapaLineItem associated with the clicked button
     */
    public void setYapaImageSelectedCallback(ImageYapaLineItem i) {
        imageSelectedCallback = i;
        Intent newImage = new Intent();
        newImage.setType("image/*");
        newImage.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
            Intent.createChooser(newImage, "Select Image"),
            YapaLineItem.SELECT_PICTURE
        );
    }

}
