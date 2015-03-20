package co.tapdatapp.tapandroid;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NdefFormatable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import co.tapdatapp.tapandroid.service.TapCloud;
import co.tapdatapp.tapandroid.service.TapTag;
import co.tapdatapp.tapandroid.service.TapYapa;
import co.tapdatapp.tapandroid.user.Account;


public class WriteActivity extends Activity {

    private String mAuthToken;
    private TapTag mTapTag;
    boolean mWriteMode = false;
    private NfcAdapter mNfcAdapter;
    private PendingIntent mNfcPendingIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);
        mTapTag = new TapTag();
        Intent intent = getIntent();
        mAuthToken = intent.getStringExtra("AuthToken");
        mTapTag.setTagID( intent.getStringExtra("TagID"));
        mTapTag.setTagName(intent.getStringExtra("TagName"));
        mTapTag.loadYapa(mAuthToken);


    }
    @Override
    public void onResume(){
        super.onResume();
        EditText edName = (EditText) findViewById(R.id.edTagName);
        if (mTapTag.getTagName().equals("null")){
            edName.setText("Name Your Tag");

        }else{
            edName.setText(mTapTag.getTagName());
        }
        TextView tvID = (TextView) findViewById(R.id.tvID);
        tvID.setText(mTapTag.getTagID());

        ArrayList<TapYapa> myYappas = mTapTag.myYappas();

        edName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus){
                    EditText g = (EditText) v;
                    try {
                        mTapTag.updateTag(mAuthToken, mTapTag.getTagID().replaceAll("-", ""), g.getText().toString());
//                    Toast.makeText(WriteActivity.this, "lost it", Toast.LENGTH_LONG);
                    }
                    catch (JSONException je) {
                        TapApplication.handleFailures(je);
                    }
                }
            }
        });

        //first Yapa
        if(myYappas.size() > 0) {
            EditText edMessage = (EditText) findViewById(R.id.dtYapaMessage);
            ImageView iv = (ImageView) findViewById(R.id.yapa1);

            edMessage.setText(myYappas.get(0).getContent());
            new TapCloud.DownloadImageTask(iv)
                    .execute(myYappas.get(0).getThumbYapa());


            edMessage.setOnFocusChangeListener( new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus){
                    if (!hasFocus){
                        EditText g = (EditText) v;
                        mTapTag.myYappas().get(0).setContent(((EditText) v).getText().toString());
                        try {
                            mTapTag.myYappas().get(0).updateYapa(mAuthToken, mTapTag.getTagID());
                        }
                        catch (JSONException je) {
                            TapApplication.handleFailures(je);
                        }
                    }

                }
            });
            //get the second on in here
            if (myYappas.size() > 1){
                EditText edMessageBonus = (EditText) findViewById(R.id.edBonusYapa);
                ImageView ivBonus = (ImageView) findViewById(R.id.yapa2);
                edMessageBonus.setText(myYappas.get(1).getContent());
                new TapCloud.DownloadImageTask(ivBonus)
                        .execute(myYappas.get(1).getThumbYapa());

                //threshhold
                //text
                //image


            }
        }

    }
    public void saveMyYapa(View v){


    }

    //Image stuff
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String mCurrentPhotoPath;

    static final int REQUEST_TAKE_PHOTO = 1;
    private TapCloud mTapCloud = new TapCloud();
    public void selectImageYapa1(View v){
        selectImage(0);
    }
    public void selectImageYapa2(View v){
        selectImage(1);
    }
    private boolean mFromCamera = false;
    private int mImageID = 0;
    private void selectImage(int ImageID) {
        mImageID = ImageID;
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };

        AlertDialog.Builder builder = new AlertDialog.Builder(WriteActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
            if (items[item].equals("Take Photo")) {
                mFromCamera = true;
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;

                    if (photoFile != null) {
//                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
//                                Uri.fromFile(photoFile));

                    }
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
                } else if (items[item].equals("Choose from Library")) {
                    mFromCamera = false;
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent , 1);//one can be replaced with any action code
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        throw new AssertionError("This should never be called");
        // @TODO this shouldn't be used any more, and this code should
        // be completely removed once the new write methods are in place.
        /*
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (mFromCamera) {
                //TODO: This only gets a thumbnail... to get full image this has to be rewritten!

                Bitmap bmp = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress( Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();


                String newFullImageURL = mTapCloud.uploadToS3withStream(byteArray, Account.getRandomString(16) + ".jpg", this);
                Bitmap thumb = Bitmap.createScaledBitmap(bmp,512,512,false);
                ByteArrayOutputStream thumbstream = new ByteArrayOutputStream();
                thumb.compress(Bitmap.CompressFormat.PNG, 100, thumbstream);
                byte[] thumbarray = thumbstream.toByteArray();
                String newThumbImageURL = mTapCloud.uploadToS3withStream(thumbarray, Account.getRandomString(16) + ".jpg", this);

                ArrayList<TapYapa> myYappas = mTapTag.myYappas();
                if(myYappas.size() > 0) {

                    EditText edMessage;
                    ImageView iv;

                    if (mImageID == 0) {
                        edMessage  = (EditText) findViewById(R.id.dtYapaMessage);
                        iv = (ImageView) findViewById(R.id.yapa1);
                        myYappas.get(0).setContent(edMessage.getText().toString());
                        myYappas.get(0).setThumbYapa(newThumbImageURL);
                        myYappas.get(0).setFullYapa(newFullImageURL);
                        try {
                            myYappas.get(0).updateYapa(mAuthToken, mTapTag.getTagID());
                        }
                        catch (JSONException je) {
                            TapApplication.handleFailures(je);
                        }
                    }
                    else {
                        edMessage  = (EditText) findViewById(R.id.edBonusYapa);
                        iv = (ImageView) findViewById(R.id.yapa2);
                        if (myYappas.size()==1){
                            //we only have the first one. We need to create a new Yapa to save
                            TapYapa newYap = new TapYapa();
                            newYap.setContent(edMessage.getText().toString());
                            newYap.setThumbYapa(newThumbImageURL);
                            newYap.setFullYapa(newFullImageURL);
                            newYap.setThreshold(5);
                            mTapTag.addYapa(mAuthToken,newYap);
                        }
                        else {
                            myYappas.get(1).setContent(edMessage.getText().toString());
                            myYappas.get(1).setThumbYapa(newThumbImageURL);
                            myYappas.get(1).setFullYapa(newFullImageURL);
                            try {
                                myYappas.get(1).updateYapa(mAuthToken, mTapTag.getTagID());
                            }
                            catch (JSONException je) {
                                TapApplication.handles(je);
                            }
                        }
                    }

                    int a= 5;
                    //  new TapCloud.DownloadImageTask(iv)
                    //        .execute(myYappas.get(0).getThumbYapa());
                }

//                String b = mCurrentPhotoPath;
//                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                File f = new File(mCurrentPhotoPath);
//                Uri contentUri = Uri.fromFile(f);
//                mediaScanIntent.setData(contentUri);
//                this.sendBroadcast(mediaScanIntent);
//                setPic(mImageID);
            }
            else {
                Uri mContentURI = data.getData();
                ImageView mImageView;
                if (mImageID == 0 ){
                    mImageView = (ImageView) findViewById(R.id.yapa1);
                }
                else {
                    mImageView = (ImageView) findViewById(R.id.yapa2);

                }
                String newFullImageURL = mTapCloud.uploadToS3withURI(mContentURI, Account.getRandomString(16) +".jpg", this);
                String newFUllImagePath = TapCloud.getRealPathFromURI(this, mContentURI);
                String newThumbImageURL = "";
                try {
                    ExifInterface exif = new ExifInterface(newFUllImagePath);
                    byte[] imageData = exif.getThumbnail();
                    Bitmap thumbnail = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    mImageView.setImageBitmap(thumbnail);

                    newThumbImageURL = mTapCloud.uploadToS3withStream(imageData, Account.getRandomString(16) + ".jpg", this);


                    ArrayList<TapYapa> myYappas = mTapTag.myYappas();
                    if(myYappas.size() > 0) {

                        EditText edMessage;
                        ImageView iv;

                        if (mImageID == 0) {
                            edMessage  = (EditText) findViewById(R.id.dtYapaMessage);
                            iv = (ImageView) findViewById(R.id.yapa1);
                            myYappas.get(0).setContent(edMessage.getText().toString());
                            myYappas.get(0).setThumbYapa(newThumbImageURL);
                            myYappas.get(0).setFullYapa(newFullImageURL);
                            myYappas.get(0).updateYapa(mAuthToken, mTapTag.getTagID());
                        }
                        else {
                            edMessage  = (EditText) findViewById(R.id.edBonusYapa);
                            iv = (ImageView) findViewById(R.id.yapa2);
                            if (myYappas.size()==1){
                                //we only have the first one. We need to create a new Yapa to save
                                TapYapa newYap = new TapYapa();
                                newYap.setContent(edMessage.getText().toString());
                                newYap.setThumbYapa(newThumbImageURL);
                                newYap.setFullYapa(newFullImageURL);
                                newYap.setThreshold(5);
                                mTapTag.addYapa(mAuthToken,newYap);
                            }
                            else {
                                myYappas.get(1).setContent(edMessage.getText().toString());
                                myYappas.get(1).setThumbYapa(newThumbImageURL);
                                myYappas.get(1).setFullYapa(newFullImageURL);
                                myYappas.get(1).updateYapa(mAuthToken, mTapTag.getTagID());
                            }
                        }

                        int a= 5;
                      //  new TapCloud.DownloadImageTask(iv)
                        //        .execute(myYappas.get(0).getThumbYapa());
                    }
                }
                catch (Exception e){
                    //TODO: not sure what to catch here?
                }

                //    String selectedImagePath = getPath(mContentURI);
                //   String newThumbImageURL = mTapCloud.uploadToS3withURI(mContentURI, TapUser.getRandomString(16), this);


//                mTapUser.setProfilePicFull(newFullImageURL );
 //               mTapUser.setProfilePicThumb(newThumbImageURL );


//                mTapUser.UpdateUser(mAuthToken);
                setPic(mImageID);
            }
        }
        */
    }
    private void setPic(int i) {
        ImageView mImageView;
        if (i==0) {
             mImageView = (ImageView) findViewById(R.id.yapa1);
        }else{

             mImageView = (ImageView) findViewById(R.id.yapa2);
            }

        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        //Bitmap bm = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }


    //MENU STUFF
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.write, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    //NFC TAG STUFF
    public void startWrite (View view){

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mNfcPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, WriteActivity.class).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        enableTagWriteMode();

        new AlertDialog.Builder(WriteActivity.this).setTitle("Touch tag to write")
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
            Tag detectedTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            // Get ID from local storage
            // write ID to tag
            String strMime = "tapdat/performer";
            String strID = mTapTag.getTagID();
            NdefRecord record = NdefRecord.createMime( strMime, strID.getBytes());
            NdefMessage message = new NdefMessage(new NdefRecord[] { record });
            if (writeDaTag(message, detectedTag)) {
                Toast.makeText(this, "Success: Wrote placeid to nfc tag", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }
    public boolean writeDaTag(NdefMessage message, Tag tag) {
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
