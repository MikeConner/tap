/**
 * Activity for updating tag name, managing rewards, and calling the
 * Activity to write a tag.
 */

package co.tapdatapp.tapandroid.tags;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.helpers.UserFriendlyError;
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

    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    boolean mWriteMode = false;

    private boolean needsSaved;
    private boolean allowUserUpdating = false;
    private Tag tag = null;
    private YapaLineItemWithImage imageSelectedCallback;

    /**
     * Interface for line items that will allow the user to select
     * an image. This allows the line item to be notified once the
     * image is selected, so it can display it and record what was
     * selected.
     */
    public interface YapaLineItemWithImage {
        void onImageSet(String imageUrl, String thumbUrl, AndroidCache cache);
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_manage_tag);
        String tagId;
        int mode;
        if (state != null) {
            tagId = state.getString(TAG_ID);
            mode = state.getInt(MODE);
        }
        else {
            Intent intent = getIntent();
            tagId = intent.getStringExtra(TAG_ID);
            mode = intent.getIntExtra(MODE, 0);
        }
        if (mode == 0) {
            throw new AssertionError("must provide a mode");
        }
        tag = new Tag();
        tag.moveTo(tagId);
        fillIn();
        needsSaved = mode == MODE_NEW;
        setActionButtonState();
        allowUserUpdating = true;
    }

    /**
     * Save whether this tag's ID has changed and whether the mode
     * has changed.
     *
     * @param b Bundle carrying the data
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle b) {
        b.putInt(MODE, needsSaved ? MODE_MODIFY : MODE_NEW);
        b.putString(TAG_ID, tag.getTagId());
        super.onSaveInstanceState(b);
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
        final EditText tagName = (EditText)findViewById(R.id.etTagName);
        //Clicking on the text field clears the field
        tagName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    tagName.setText(" ");
            }
        });
        if (tag.getTagId() != null) {
            tagName.setText(tag.getName());
            ListView yapaList = (ListView)findViewById(R.id.listYapa);
            YapaAdapter adapter = new YapaAdapter(this, tag);
            yapaList.setAdapter(adapter);
        }
        tagName.addTextChangedListener(this);
    }

    /**
     * Enable/disable the write and save buttons as appropriate
     */
    private void setActionButtonState() {
        findViewById(R.id.btnWriteTag).setEnabled(
            !Tag.NEW_TAG_ID.equals(tag.getTagId())
        );
        findViewById(R.id.btnSaveTag).setEnabled(needsSaved);
    }

    /**
     * Called any time data is changed by the user. Sets the
     * modified flag to true and updates any interface widgets as
     * needed. The two challenges to making this work are first, this
     * gets called repeatedly when the view is being created by
     * Android, which is why allowUserUpdating is required to be
     * false until the view is fully composited; and the fact that
     * many operations will appear to change something without the
     * actual data itself being modified, which is what the
     * changed parameter indicates.
     */
    public void onChange(boolean changed) {
        if (allowUserUpdating && changed) {
            needsSaved = true;
        }
        setActionButtonState();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // do nothing
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Do nothing
    }

    /**
     * When text changes, update the modified status
     */
    @Override
    public void afterTextChanged(Editable s) {
        onChange(tag.setNameIfChanged(s.toString()));
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
        y.setType(tag.getYapa()[0].getType());
        tag.addYapa(y);
        fillIn();
        onChange(true);
    }

    /**
     * Called when the SAVE button is tapped to save to the server
     */
    public void onClickSave(View view) {
        findViewById(R.id.btnWriteTag).setEnabled(false);
        saveTagAndYapaToSQL();
        new SaveTagToServerTask().execute(this, tag.getTagId());
    }

    /**
     * Take what's in the Tag object and save it to SQLite
     */
    private void saveTagAndYapaToSQL() {
        tag.setName(((EditText)findViewById(R.id.etTagName)).getText().toString());
        tag.update();
        Yapa[] yList = tag.getYapa();
        yList[0].remove(tag.getTagId());
        for (Yapa y : yList) {
            y.create();
        }
    }

    /**
     * Callback when updating the tag on the server is complete
     *
     * @param tagId Tag ID of what was saved, will be different if a new tag was created
     */
    @Override
    public void onTagSaved(String tagId) {
        needsSaved = false;
        tag.setTagId(tagId);
        for (Yapa y : tag.getYapa()) {
            y.setTagId(tagId);
        }
        // Remove any old tag related to this and completely replace it
        tag.remove(Tag.NEW_TAG_ID);
        tag.remove(tag.getTagId());
        tag.create(tag.getTagId(), tag.getName(), tag.getCurrencyId(), tag.getYapa());
        Toast t = Toast.makeText(this, R.string.tag_was_saved, Toast.LENGTH_LONG);
        t.show();
        setActionButtonState();
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
                    AndroidCache cache = new AndroidCache();
                    String thumbUrl;
                    String imageUrl;
                    InputStream imageStream = null;
                    try {
                        Uri streamUrl = data.getData();
                        imageStream = getContentResolver().openInputStream(streamUrl);
                        thumbUrl = TapBitmap.storeThumbnailLocal(imageStream, 512);
                        imageStream.close();
                        imageStream = getContentResolver().openInputStream(streamUrl);
                        imageUrl = TapBitmap.storeThumbnailLocal(imageStream, 1024);
                    }
                    catch (Throwable t) {
                        TapApplication.errorToUser(TapApplication.string(R.string.file_access_problem));
                        Log.wtf("IMAGE", t);
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
                    imageSelectedCallback.onImageSet(imageUrl, thumbUrl, cache);
                    imageSelectedCallback = null;
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
     * @param i the Yapa line item associated with the clicked button
     */
    public void setYapaImageSelectedCallback(YapaLineItemWithImage i) {
        imageSelectedCallback = i;
        Intent newImage = new Intent();
        newImage.setType("image/*");
        newImage.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
            Intent.createChooser(newImage, "Select Image"),
            YapaLineItem.SELECT_PICTURE
        );
    }

    /**
     * Start the process of writing a tag (initiated by click)
     *
     * @param view The WRITE button
     */
    public void onClickWrite(View view) {

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, ManageTagActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        enableTagWriteMode();

        new AlertDialog.Builder(ManageTagActivity.this).setTitle("Touch tag to write")
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        disableTagWriteMode();
                    }

                }).create().show();
    }

    /**
     *
     */
    private void enableTagWriteMode() {
        mWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] mWriteTagFilters = new IntentFilter[] { tagDetected };
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
    }

    /**
     *
     */
    private void disableTagWriteMode() {
        mWriteMode = false;
        mNfcAdapter.disableForegroundDispatch(this);
    }

    /**
     *
     * @param intent The intent that started this Activity
     */
    @Override
    protected void onNewIntent(Intent intent) {
        // Tag writing mode
        if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            android.nfc.Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String strID = tag.getTagId();
            NdefRecord record = NdefRecord.createUri("http://tapnology.co/tag/" + strID);
            NdefMessage message = new NdefMessage(new NdefRecord[] { record });
            try {
                writeTag(message, detectedTag);
                Toast t = Toast.makeText(this, R.string.tag_write_success, Toast.LENGTH_LONG);
                t.show();
            }
            catch (Exception ioe) {
                TapApplication.handleFailures(ioe);
            }
        }
        else {
            Log.d("FLOW", "onNewIntent() not ACTION_TAG_DISCOVERED");
        }
    }

    /**
     *
     */
    public void
    writeTag(NdefMessage message, android.nfc.Tag tag)
    throws FormatException, IOException, UserFriendlyError {
        int size = message.toByteArray().length;
        Ndef ndef = Ndef.get(tag);
        if (ndef != null) {
            ndef.connect();
            if (!ndef.isWritable()) {
                throw new UserFriendlyError(
                    TapApplication.string(R.string.tag_read_only)
                );
            }
            if (ndef.getMaxSize() < size) {
                throw new UserFriendlyError(
                    TapApplication.string(R.string.tag_too_small)
                );
            }
            ndef.writeNdefMessage(message);
        }
        else {
            NdefFormatable format = NdefFormatable.get(tag);
            if (format == null) {
                throw new FormatException(
                    TapApplication.string(R.string.tag_format_failed)
                );
            }
            format.connect();
            format.format(message);
        }
    }

}
