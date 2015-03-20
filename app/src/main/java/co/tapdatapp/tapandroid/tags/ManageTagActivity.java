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
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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


    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;
    boolean mWriteMode = false;


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
        onChange();
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
        onChange();
    }

    /**
     * Called when the SAVE button is tapped to save to the server
     */
    public void onClickSave(View view) {
        findViewById(R.id.btnWriteTag).setEnabled(false);
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
        needsSaved = false;
        setActionButtonState();
        Toast t = Toast.makeText(this, R.string.tag_was_saved, Toast.LENGTH_LONG);
        t.show();
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










    //NFC TAG STUFF
    public void startWrite (View view){

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


        //  Intent i = new Intent(this, WriteActivity.class);
        //  startActivity(i);

    }
    private void enableTagWriteMode() {
        mWriteMode = true;
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        IntentFilter[] mWriteTagFilters = new IntentFilter[] { tagDetected };
        mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mWriteTagFilters, null);
    }
    private void disableTagWriteMode() {
        mWriteMode = false;
        mNfcAdapter.disableForegroundDispatch(this);
    }
    @Override
    protected void onNewIntent(Intent intent) {
        // Tag writing mode
        if (mWriteMode && NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            android.nfc.Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // Get ID from local storage
            // write ID to tag
            //String strMime = "tapdat/performer";
            String strID = tag.getTagId();
            //NdefRecord record = NdefRecord.createMime( strMime, strID.getBytes());
            NdefRecord record = NdefRecord.createUri("http://tapnology.co/nfc_tags/" + strID);
            NdefMessage message = new NdefMessage(new NdefRecord[] { record });
            if (writeDaTag(message, detectedTag)) {
                Toast.makeText(this, "Success: Wrote placeid to nfc tag", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }
    public boolean writeDaTag(NdefMessage message, android.nfc.Tag tag) {
        int size = message.toByteArray().length;
        try {
            Ndef ndef = Ndef.get(tag);
            if (ndef != null) {
                ndef.connect();
                if (!ndef.isWritable()) {
                    Toast.makeText(getApplicationContext(),
                            "Error: tag not writable",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (ndef.getMaxSize() < size) {
                    Toast.makeText(getApplicationContext(),
                            "Error: tag too small",
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
                ndef.writeNdefMessage(message);
                return true;
            } else {
                NdefFormatable format = NdefFormatable.get(tag);
                if (format != null) {
                    try {
                        format.connect();
                        format.format(message);
                        return true;
                    } catch (IOException e) {
                        return false;
                    }
                } else {
                    return false;
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

}
