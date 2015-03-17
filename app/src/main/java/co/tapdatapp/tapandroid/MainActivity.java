package co.tapdatapp.tapandroid;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Environment;
import android.os.Parcelable;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import co.tapdatapp.tapandroid.arm.ArmFragment;
import co.tapdatapp.tapandroid.arm.ArmedFragment;
import co.tapdatapp.tapandroid.helpers.DevHelper;
import co.tapdatapp.tapandroid.helpers.UserFriendlyError;
import co.tapdatapp.tapandroid.history.HistoryFragment;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.localdata.Transaction;
import co.tapdatapp.tapandroid.remotedata.TapTxnTask;
import co.tapdatapp.tapandroid.service.TapCloud;
import co.tapdatapp.tapandroid.service.TapUser;
import co.tapdatapp.tapandroid.service.TapTxn;
import co.tapdatapp.tapandroid.tags.TagsFragment;
import co.tapdatapp.tapandroid.user.Account;
import co.tapdatapp.tapandroid.user.AccountFragment;
import co.tapdatapp.tapandroid.yapa.YapaDisplay;

public class MainActivity
extends Activity
implements DepositBTCFragment.OnFragmentInteractionListener,
           ActionBar.TabListener,
           TapTxnTask.TapTxnInitiator {

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    protected TapUser mTapUser;
    protected TapCloud mTapCloud;

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

    CurrencyDAO currency;

    /**
     * For tapping, store the desired transaction object to be
     * referenced during background task execution
     */
    TapTxn outgoingTransaction = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        captureNFC();
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

    /**
     * If no account, fire up the account creation screen, otherwise,
     * start the app up.
     */
    @Override
    protected void onStart() {
        super.onStart();
        Account account = new Account();
        if (!account.created()) {
            Intent intent = new Intent(this, AccountStartActivity.class);
            startActivity(intent);
        }
        else {
            setupTabs();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void setupTabs(){
        //TODO: In teh case where balance is zero open up a load phone fragment
        if (mSectionsPagerAdapter == null) {
            setContentView(R.layout.activity_main);
            // Create the adapter that will return a fragment for each of the three
            // primary sections of the activity.

            //set up action bar and nav tabs
            mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager(), this);
            // Set up the action bar.
            final ActionBar actionBar = getActionBar();
            assert actionBar != null;
            actionBar.setDisplayShowTitleEnabled(false);

            // Specify that the Home/Up button should not be enabled, since there is no hierarchical
            // parent.
//            actionBar.setHomeButtonEnabled(false);
            // Specify that we will be displaying tabs in the action bar.
            //actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            // Set up the ViewPager with the sections adapter.
            mViewPager = (ViewPager) findViewById(R.id.pager);
            mViewPager.setAdapter(mSectionsPagerAdapter);
            mViewPager.setCurrentItem(2);
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {

    }

    @Override
    public void onResume(){
        super.onResume();
        currency = new CurrencyDAO();
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
        mArmFrag =  new ArmedFragment();
        mArmFrag.show(ft, "armed");
    }

    private View randomTransactionButton;

    /**
     * For development builds only, generate a random tag ID to do a transaction with
     */
    public void clickRandomTransaction(View v) {
        if (!DevHelper.isEnabled(R.string.CREATE_FAKE_DATA_ON_SERVER)) {
            throw new AssertionError("Dev commands issued on dev-disabled build");
        }
        randomTransactionButton = v;
        outgoingTransaction = new TapTxn();
        outgoingTransaction.setTagID("XX" + UUID.randomUUID().toString().replaceAll("-", "").substring(7, 15));
        outgoingTransaction.setTxnAmount(new Account().getArmedAmount());
        outgoingTransaction.setCurrencyId(new Account().getActiveCurrency());
        Log.d("TAP", "Phoney Transaction starting");
        new TapTxnTask().execute(this);
    }

    /**
     * For development builds only, tap the entered tag ID
     */
    public void clickEnteredTransaction(String tag) {
        if (!DevHelper.isEnabled(R.string.CREATE_FAKE_DATA_ON_SERVER)) {
            throw new AssertionError("Dev commands issued on dev-disabled build");
        }
        outgoingTransaction = new TapTxn();
        outgoingTransaction.setTagID(tag);
        outgoingTransaction.setTxnAmount(new Account().getArmedAmount());
        outgoingTransaction.setCurrencyId(new Account().getActiveCurrency());
        Log.d("TAP", "entered Transaction starting");
        new TapTxnTask().execute(this);
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
                    outgoingTransaction = new TapTxn();
                    outgoingTransaction.setTagID(result.replaceAll("-", ""));
                    outgoingTransaction.setTxnAmount(new Account().getArmedAmount());
                    outgoingTransaction.setCurrencyId(new Account().getActiveCurrency());
                    new TapTxnTask().execute(this);

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

    @Override
    public TapTxn getTapTxn() {
        return outgoingTransaction;
    }

    /**
     * Called when the webservice has responded to a transaction
     */
    @Override
    public void onTapNetComplete(Transaction t) {
        mArmed = false;
        String mMessage = outgoingTransaction.getMessage();
        mArmFrag.updateWithResult(mMessage);
        outgoingTransaction = null;
        if (randomTransactionButton != null) {
            randomTransactionButton.setEnabled(true);
            randomTransactionButton = null;
        }
        /**
         * This opens the new Yapa page after a transaction
         */
        Intent openYapa = new Intent(
            this,
            new YapaDisplay().getDisplayClass(t)
        );
        openYapa.putExtra(YapaDisplay.TRANSACTION_ID, t.getSlug());
        openYapa.putExtra(YapaDisplay.DELAY_TIME, 5);
        startActivity(openYapa);
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

    //generic stuff for fragments
    public   TapUser getUserContext(){
        return mTapUser;
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
        Context mContext;

        public SectionsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {

            Fragment frag;
            switch (position) {
                case 0:
                    frag = new TagsFragment();
                    break;
                case 1:
                    frag = new AccountFragment();
                    break;
                case 2:
                    frag = new ArmFragment();
                    break;
                case 3:
                    frag = new HistoryFragment();
                    break;

                default: throw new IllegalArgumentException("Invalid Section Number");
            }
            return frag;

        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Locale l = Locale.getDefault();
            switch (position) {
                case 0:
                    return getString(R.string.title_tags).toUpperCase(l);
                case 1:
                    return getString(R.string.title_account).toUpperCase(l);
                case 2:
                    return getString(R.string.title_tap).toUpperCase(l);
                case 3:
                    return getString(R.string.title_history).toUpperCase(l);
            }
            return null;
        }
    }

    /**
     * Exits the app on back press
     */
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        // we need this for fragments / menus
        //not sure what we have to do here if anything
    }

}
