package co.tapdatapp.tapandroid;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Parcelable;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v13.app.FragmentPagerAdapter;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import co.tapdatapp.tapandroid.service.TapCloud;
import co.tapdatapp.tapandroid.service.TapUser;
import co.tapdatapp.tapandroid.service.TapTxn;
import co.tapdatapp.tapandroid.service.YapaAdapter;
import de.hdodenhof.circleimageview.CircleImageView;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;




public class MainActivity extends Activity implements AccountFragment.OnFragmentInteractionListener, HistoryFragment.OnFragmentInteractionListener, ArmFragment.OnFragmentInteractionListener, ActionBar.TabListener, DataLoaderFragment.ProgressListener {
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    protected SharedPreferences mPreferences;
    protected String mPhoneSecret;
    protected String mAuthToken;

    protected TapUser mTapUser;
    protected TapCloud mTapCloud;

    protected float fAmount;
    protected int fUnit;

    private TextView txAmount;

    private NfcAdapter mNfcAdapter;
    private IntentFilter[] mNdefExchangeFilters;
    private PendingIntent mNfcPendingIntent;

    private boolean mArmed = false;
    private ArmedFragment mArmFrag;

    //For File Uploads
    static final int REQUEST_IMAGE_CAPTURE = 1;
    String mCurrentPhotoPath;
    boolean mFromCamera = false;
    static final int REQUEST_TAKE_PHOTO = 1;


    //splash screen and data loader fragment
    private static final String TAG_DATA_LOADER = "TAPloader";
    private static final String TAG_SPLASH_SCREEN = "TAPsplashScreen";

    private DataLoaderFragment mDataLoaderFragment;
    private SplashScreenFragment mSplashScreenFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Default Amounts / Units for Tap Screen
        fAmount = 1;
        fUnit = 1;
        captureNFC();

        //TODO: REMOVE THIS - Make sure all NETWORK TXNS are async
        //StrictMode.ThreadPolicy tp = StrictMode.ThreadPolicy.LAX;
        //StrictMode.setThreadPolicy(tp);

        super.onCreate(savedInstanceState);

    }

    //TODO: Move this to DataLoaderFragment
    private void captureNFC(){
        //Capture NFC interactions for this activity
        //TODO: make sure NFC is turned on or kill the APP with dialog
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
        IntentFilter tapFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            tapFilter.addDataType("tapdat/performer");    /* Handles all MIME based dispatches.
                                       You should specify only the ones that you need. */
        }
        catch (IntentFilter.MalformedMimeTypeException  e) {
            throw new RuntimeException("fail", e);
        }
        mNdefExchangeFilters = new IntentFilter[] { tapFilter };
    }


    @Override
    protected void onStart() {
        super.onStart();




        //Load Splash Fragment
        final FragmentManager fm = getFragmentManager();
        mDataLoaderFragment = (DataLoaderFragment) fm.findFragmentByTag(TAG_DATA_LOADER);
        if (mDataLoaderFragment == null) {
            mDataLoaderFragment = new DataLoaderFragment();
            mDataLoaderFragment.setProgressListener(this);
            mDataLoaderFragment.startLoading();
            fm.beginTransaction().add(mDataLoaderFragment, TAG_DATA_LOADER).commit();
        } else {
            if (checkCompletionStatus()) {
                return;
            }
        }

        // Show loading fragment
        mSplashScreenFragment = (SplashScreenFragment) fm.findFragmentByTag(TAG_SPLASH_SCREEN);
        if (mSplashScreenFragment == null) {
            mSplashScreenFragment = new SplashScreenFragment();
            fm.beginTransaction().add(android.R.id.content, mSplashScreenFragment, TAG_SPLASH_SCREEN).commit();
        }

        if (mDataLoaderFragment != null) {
            checkCompletionStatus();
        }
    }

    @Override
    public void onCompletion(Double result) {
        setupTabs();
        mDataLoaderFragment = null;
    }



    @Override
    public void onProgressUpdate(int progress) {
        mSplashScreenFragment.setProgress(progress);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mDataLoaderFragment != null) {
            mDataLoaderFragment.removeProgressListener();
        }
    }
    /**
     * Checks if data is done loading, if it is, the result is handled
     *
     * @return true if data is done loading
     */
    private boolean checkCompletionStatus() {
        if (mDataLoaderFragment.hasResult()) {
            onCompletion(mDataLoaderFragment.getResult());
            FragmentManager fm = getFragmentManager();
            mSplashScreenFragment = (SplashScreenFragment) fm.findFragmentByTag(TAG_SPLASH_SCREEN);
            if (mSplashScreenFragment != null) {
                fm.beginTransaction().remove(mSplashScreenFragment). commit();
            }
            return true;
        }
        mDataLoaderFragment.setProgressListener(this);
        return false;
    }


    private void setupTabs(){
        //TODO: In teh case where balance is zero open up a load phone fragment

        setContentView(R.layout.activity_main);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        //set up action bar and nav tabs
        mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        // Specify that the Home/Up button should not be enabled, since there is no hierarchical
        // parent.
        actionBar.setHomeButtonEnabled(false);
        // Specify that we will be displaying tabs in the action bar.
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // When swiping between different app sections, select the corresponding tab.
                // We can also use ActionBar.Tab#select() to do this if we have a reference to the
                // Tab.
                actionBar.setSelectedNavigationItem(position);
            }
        });
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by the adapter.
            // Also specify this Activity object, which implements the TabListener interface, as the
            // listener for when this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }

        //sets home page to tap

        mViewPager.setCurrentItem(1);
    }


    @Override
    public void onResume(){
        super.onResume();

        txAmount = (TextView) findViewById(R.id.txtAmount);

        if (mNfcAdapter != null) {
            mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent,
                    mNdefExchangeFilters, null);
            //if (!mNfcAdapter.isEnabled()) {
            //LayoutInflater inflater = getLayoutInflater();
            //View dialoglayout = inflater.inflate(R.layout.nfc_settings_layout,(ViewGroup) findViewById(R.id.nfc_settings_layout));
                /*new AlertDialog.Builder(this).setView(dialoglayout)
                        .setPositiveButton("Update Settings", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent setnfc = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(setnfc);
                            }
                        })
                        .setOnCancelListener(new DialogInterface.OnCancelListener() {
                            public void onCancel(DialogInterface dialog) {
                                finish(); // exit application if user cancels
                            }
                        }).create().show();
            }*/
            // } else {
            //     Toast.makeText(getApplicationContext(), "Sorry, No NFC Adapter found.", Toast.LENGTH_SHORT).show();
            // }
        }

    }
    @Override
    public void onPause(){
        super.onPause();
        if(mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
    }




    //Image Stuff
    public void getImage(View view){
        selectImage();
    }
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }
    private void selectImage() {
        final CharSequence[] items = { "Take Photo", "Choose from Library",
                "Cancel" };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    mFromCamera = true;
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                        } catch (IOException ex) { //catch file creation issues?
                        }
                        // Continue only if the File was successfully created
                        if (photoFile != null) {
//                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
  //                                  Uri.fromFile(photoFile));
                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                        }

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
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            if (mFromCamera) {
             //TODO: This only gets a shitty tumbnail right now!
              Bitmap bmp = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bmp.compress( Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();


                String newFullImageURL = mTapCloud.uploadToS3withStream(byteArray, TapUser.getRandomString(16) + ".jpg", this);
                Bitmap thumb = Bitmap.createScaledBitmap(bmp,512,512,false);
                ByteArrayOutputStream thumbstream = new ByteArrayOutputStream();
                thumb.compress(Bitmap.CompressFormat.PNG, 100, thumbstream);
                byte[] thumbarray = thumbstream.toByteArray();
                String newThumbImageURL = mTapCloud.uploadToS3withStream(thumbarray, TapUser.getRandomString(16) + ".jpg", this);


  //              BitmapFactory.Options bmOptions = new BitmapFactory.Options();
  //              bmOptions.inJustDecodeBounds = true;
 //               int targetW = 512;
 //               int targetH = 512;
  //              int photoW = bmOptions.outWidth;
   //             int photoH = bmOptions.outHeight;
                //                BitmapFactory.decode (mCurrentPhotoPath, bmOptions);
//                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
//                File f = new File(mCurrentPhotoPath);
//                Uri contentUri = Uri.fromFile(f);
//                mediaScanIntent.setData(contentUri);
//                this.sendBroadcast(mediaScanIntent);
//                BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                // Determine how much to scale down the image
//                int scaleFactor = Math.min(photoW/targetW, photoH/targetH);
//                bmOptions.inJustDecodeBounds = false;
//                bmOptions.inSampleSize = scaleFactor;
//                bmOptions.inPurgeable = true;

        //        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
                //THIS IS NULL!!
                //mCurrentPhotoPath
  //              setPic();
         //       String newFullImageURL = mTapCloud.uploadToS3withURI(contentUri, TapUser.getRandomString(16) +".jpg", this);
         //       String newFUllImagePath = TapCloud.getRealPathFromURI(this,contentUri);
        //        String newThumbImageURL = "";
   //             try {
         //           ExifInterface exif = new ExifInterface(newFUllImagePath);
         //           byte[] imageData = exif.getThumbnail();
         //           Bitmap thumbnail = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    //mImageView.setImageBitmap(thumbnail);
         //           newThumbImageURL = mTapCloud.uploadToS3withStream(imageData, TapUser.getRandomString(16) + ".jpg", this);
       //         }
     //           catch (Exception e){
      //              //TODO: not sure what to catch here?
      //          }
                mTapUser.setProfilePicFull(newFullImageURL );
                mTapUser.setProfilePicThumb(newThumbImageURL );
                mTapUser.UpdateUser(mAuthToken);
            }
            else {
                Uri mContentURI = data.getData();
                //ImageView mImageView = (ImageView) findViewById(R.id.profile_image);
                String newFullImageURL = mTapCloud.uploadToS3withURI(mContentURI, TapUser.getRandomString(16) +".jpg", this);
                String newFUllImagePath = TapCloud.getRealPathFromURI(this,mContentURI);
                String newThumbImageURL = "";
                try {
                    ExifInterface exif = new ExifInterface(newFUllImagePath);
                    byte[] imageData = exif.getThumbnail();
                    Bitmap thumbnail = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
                    //mImageView.setImageBitmap(thumbnail);
                     newThumbImageURL = mTapCloud.uploadToS3withStream(imageData, TapUser.getRandomString(16) + ".jpg", this);
                }
                catch (Exception e){
                    //TODO: not sure what to catch here?
                }
                mTapUser.setProfilePicFull(newFullImageURL );
                mTapUser.setProfilePicThumb(newThumbImageURL );
                mTapUser.UpdateUser(mAuthToken);
            }
        }
    }
    private void setPic() {
        CircleImageView mImageView = (CircleImageView) findViewById(R.id.profile_image);
        // Get the dimensions of the View
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


    //ARM SCREEN
    public void armOrSend(View v){
        mArmed=true;
        showArmedDialog();
        //TODO: make sure we unarm on resume
    }
    void showArmedDialog() {
     //  mStackLevel++;

        // DialogFragment.show() will take care of adding the fragment
        // in a transaction.  We also want to remove any currently showing
        // dialog, so make our own transaction and take care of that here.
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("armed");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        mArmFrag =  new ArmedFragment(mAuthToken, fAmount);
        mArmFrag.show(ft, "armed");
    }
    private void changeAmount(int change_value, boolean addition){
        if (addition) {
            fUnit = change_value;
            fAmount += fUnit;

            if (fAmount > 500 ) {fAmount = 500;} //MAX VALUE FOR TIP
            txAmount = (TextView) findViewById(R.id.txtAmount);
            txAmount.setText("$" + String.valueOf(fAmount));
        }
        else{
            fUnit = change_value;
            fAmount-= fUnit;
            if (fAmount < 1) {fAmount = 1;}
            txAmount = (TextView) findViewById(R.id.txtAmount);
            txAmount.setText("$" + String.valueOf(fAmount));
        }


    }
    public void tapPlus(View v){
        changeAmount(fUnit, true);
    }
    public void tapMinus(View v){
        changeAmount(fUnit, false);
    }
    private void selectMe(View v){
        Button btnOne = (Button) findViewById(R.id.btnOne);
        Button btnFive = (Button) findViewById(R.id.btnFive);
        Button btnTen = (Button) findViewById(R.id.btnTen);
        //Button btnTwenty = (Button) findViewById(R.id.btnTwenty);
        //Button btnFifty = (Button) findViewById(R.id.btnFifty);
        //Button btnHundred = (Button) findViewById(R.id.btnHundred);

        //Resources res = getResources();
        //Drawable selected = res.getDrawable(R.drawable.circleselected);
        //Drawable normal = res.getDrawable(R.drawable.circle);
        //btnOne.setBackground(normal);
        ///btnFive.setBackground(normal);
        //btnTen.setBackground(normal);
        //btnTwenty.setBackground(normal);
        //btnFifty.setBackground(normal);
        //btnHundred.setBackground(normal);

        //v.setBackground(selected);

    }
    public void tapOne(View v){
        selectMe(v);
        changeAmount(1,true);
    }
    public void tapFive(View v){
        selectMe(v);
        changeAmount(5,true);


    }
    public void tapTen(View v){
        selectMe(v);

        changeAmount(10,true);


    }
    public void tapTwenty(View v){
        selectMe(v);

        changeAmount(20, true);

    }
    public void tapFifty(View v){
        selectMe(v);

        changeAmount(50,true);


    }
    public void tapHundred(View v){
        selectMe(v);

        changeAmount(100,true);


    }

    //NFC STUFF
    @Override
    protected void onNewIntent(Intent intent) {

        //TODO: WHen not in armed mode, if intent is detected, change to send mode
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            NdefMessage[] messages = null;
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                messages = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    messages[i] = (NdefMessage) rawMsgs[i];
                }
            }
            if(messages[0] != null) {
                String result="";
                byte[] payload = messages[0].getRecords()[0].getPayload();
                // this assumes that we get back am SOH followed by host/code
                for (int b = 0; b<payload.length; b++) { // skip SOH
                    result += (char) payload[b];
                }

                if (mArmed){
                    TapTxn txn = new TapTxn();
                    txn.setAuthToken(mAuthToken);
                    txn.setTagID(result.replaceAll("-",""));
                    txn.setTxnAmount(fAmount);
                    new executeTapTxn().execute(txn);

                    mArmFrag.setValues(mMessage, mYapURL);
//                    Toast.makeText(MainActivity.this, result.getPayloadImageThumb(), Toast.LENGTH_LONG).show();

                    //tv.setText(txn.getMessage());

                } else {
                    Toast.makeText(getApplicationContext(), "Tag Contains " + result, Toast.LENGTH_SHORT).show();

                    //show tap screen, change button to SEND?
                }
                //Intent i = new Intent(this, TapArm.class);
              //  i.putExtra("TAGID", result);
              //  i.putExtra("TIPAMOUNT", fltTipAmount);
              //  startActivity(i);

            }
        }
    }
    protected String mMessage;
    protected String mYapURL;

    private class executeTapTxn extends AsyncTask<TapTxn, Integer, TapTxn> {
        protected TapTxn doInBackground(TapTxn... taptxn) {
            int count = taptxn.length;
            for (int i = 0; i < count; i++) {
                taptxn[i].TapAfool();
//                publishProgress((int) ((i / (float) count) * 100));
                // Escape early if cancel() is called
                if (isCancelled()) break;
            }
            return taptxn[0];
        }

        protected void onProgressUpdate(Integer... progress) {
            //setProgressPercent(progress[0]);
        }

        protected void onPostExecute(TapTxn result) {
            mArmed = false;
            mMessage = result.getMessage();
            mYapURL = result.getPayloadImageThumb();


        }
    }

    //HISTORY STUFF
    private void loadTxnHistory(){
        TapUser mTapUser = TapCloud.getTapUser(MainActivity.this);
        mTapUser.loadTxns(TapCloud.getAuthToken());
        //mtagMap = mTapUser.getTags(mAuthToken);
        GridView gridview = (GridView)  findViewById(R.id.gridHistory);
        //ImageAdapter imgAdp = new ImageAdapter(MainActivity.this, mTapUser.myTransactions());
        YapaAdapter imgAdp = new YapaAdapter(MainActivity.this, mTapUser.myTransactions());

        gridview.setAdapter(imgAdp);
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Toast.makeText(MainActivity.this, "" + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
                //writeThisTag(mAuthToken, parent.getItemAtPosition(position).toString());
            }
        });



    }



    private class ImageAdapter extends BaseAdapter {
        private Context mContext;
        ArrayList<TapTxn> mTapTxns;
        private String[] mTagKeys;
        private String[] mTagValues;

        public ImageAdapter(Context c, ArrayList<TapTxn> mTxns){
            mTapTxns = mTxns;
//            int i  = 0;
//            for(Map.Entry<String,String> entry : mTagMapImg.entrySet()){
//                mTagKeys[i]= (entry.getKey());
//                mTagValues[i] =  entry.getValue();
//                i++;
//            }
//
            mContext = c;

        }
        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mTapTxns.size();

        }

        public Object getItem(int position) {
            //return mTapTxns.get(position).toString();
            return "bob";
        }

        public long getItemId(int position) {
            return 0;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(300, 300));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            imageView.setImageResource(R.drawable.ic_launcher);
            return imageView;
        }


    }

    //ACCOUNT Stuff
    public void showDeposit(View view){
        //show fragment with qr code and details
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("deposit");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.

        DepositFragment mDepositFrag =  new DepositFragment();
        mDepositFrag.show(ft, "deposit");
    }
    public void showWithdraw(View view){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("withdraw");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.

        WithdrawFragment mWithdrawFrag =  new WithdrawFragment();
        mWithdrawFrag.show(ft, "withdraw");

    }

    public void myTags(View view){
        TagsFragment mTags = new TagsFragment();
        Intent i = new Intent(this,TagActivity.class);
        i.putExtra("AuthToken", mAuthToken);
        startActivity(i);

    }
    public void newNickNameMe(View view){
        TextView et = (TextView) findViewById(R.id.etNickName);
        et.setText(        mTapUser.getNewNickname(mAuthToken));

    }
    public void writeUser(View view){
        EditText edName = (EditText) findViewById(R.id.etNickName);
        EditText edEmail = (EditText) findViewById(R.id.etEmail);
        //EditText edWithDraw = (EditText) findViewById(R.id.etWithdraw);
        mTapUser.setNickName(edName.getText().toString());
        //mTapUser.setBTCoutbound( edWithDraw.getText().toString());
        mTapUser.setEmail(edEmail.getText().toString());
        mTapUser.UpdateUser(mAuthToken);

    }

    //generic stuff for fragments
    public   TapUser getUserContext(){
        return mTapUser;
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
        if(tab.getText().equals("HISTORY")){
            loadTxnHistory();
        }
    }
    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.activity_arm, menu);
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
    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {

            Fragment frag;
            switch (position) {
                case 0:
                    frag = new AccountFragment().newInstance();
                    //frag = mAccountFrag;
                    break;
                case 1:
                    frag = new ArmFragment().newInstance();
                    // frag = mTapFrag;
                    break;
                case 2:
                    frag = new HistoryFragment().newInstance();
                    //frag = mHistoryFrag ;
                    break;

                default: throw new IllegalArgumentException("Invalid Section Number");
            }
            return frag;

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_section1).toUpperCase(l);
                case 1:
                    return getString(R.string.title_section2).toUpperCase(l);
                case 2:
                    return getString(R.string.title_section3).toUpperCase(l);
            }
            return null;
        }
    }
    @Override
    public void onFragmentInteraction(Uri uri) {
        // we need this for fragments / menus
        //not sure what we have to do here if anything
    }


    // NO LONGER USED -> MOVED TO LOADER FRAGMENT
    private void tap_init(){

        mPreferences = getSharedPreferences("CurrentUser", MODE_PRIVATE);
        mTapCloud = new TapCloud();
        mTapUser = TapCloud.getTapUser(this);
        if (mPreferences.contains("PhoneSecret")) {
            mPhoneSecret = mPreferences.getString("PhoneSecret", "");
        }
        else {
            mPhoneSecret =  mTapUser.generatePhoneSecret();
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putString("PhoneSecret", mPhoneSecret);
            editor.commit();
        }
        // at this point we have a Phone Secret, let's try some network shit

        Boolean mNetwork = mTapCloud.isNetworkAvailable(this);
        if (mNetwork) {

            if (mPreferences.contains("AuthToken")) {
                if (!mPreferences.getString("AuthToken", "").isEmpty()){
                    mAuthToken = mPreferences.getString("AuthToken", "");
                    mTapCloud.setAuthToken(mAuthToken);
                    mTapUser.LoadUser(mAuthToken);
                    //TODO: Failure case for when auth token has expired -> get error, get new auth token based on secret
                    //TODO: Failure case in case we can't get to tap or tap is down
                }
                else {
                    mAuthToken =  mTapUser.CreateUser(mPhoneSecret);
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putString("AuthToken", mAuthToken);
                    //editor.putString("NickName", mTapUser.getNickname());
                    editor.commit();
                    mTapCloud.setAuthToken(mAuthToken);


                }
            }
            else{
                //Get Auth Token
                mAuthToken =  mTapUser.CreateUser(mPhoneSecret);
                SharedPreferences.Editor editor = mPreferences.edit();
                editor.putString("AuthToken", mAuthToken);
                //editor.putString("NickName", mTapUser.getNickname());
                editor.commit();


                //We know user is null, but let's load user anyway to be consistent with above
                mTapCloud.setAuthToken(mAuthToken);
                mTapUser.LoadUser(mAuthToken);

                //TODO: Delete Auth Token on kill of application, so it gets a new one when it comes back OR NOT?
            }
        }
        else {
            Toast.makeText(this, (CharSequence) ("No NETWORK!  Going Home!"), Toast.LENGTH_SHORT).show();
            //TODO: Code to send message, kill app, or figure out what to do next?
        }
        //end of Tap network Ops
    }

}
