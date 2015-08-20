package co.tapdatapp.tapandroid;

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
import android.graphics.Typeface;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import co.tapdatapp.tapandroid.arm.ArmFragment;
import co.tapdatapp.tapandroid.arm.ArmedFragment;
import co.tapdatapp.tapandroid.arm.WrongCurrencyException;
import co.tapdatapp.tapandroid.currency.BalanceList;
import co.tapdatapp.tapandroid.helpers.CustomViewPager;
import co.tapdatapp.tapandroid.helpers.DevHelper;
import co.tapdatapp.tapandroid.history.HistoryFragment;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.localdata.Transaction;
import co.tapdatapp.tapandroid.remotedata.TapTxnTask;
import co.tapdatapp.tapandroid.service.TapTxn;
import co.tapdatapp.tapandroid.tags.TagsFragment;
import co.tapdatapp.tapandroid.user.Account;
import co.tapdatapp.tapandroid.user.AccountFragment;
import co.tapdatapp.tapandroid.yapa.YapaDisplay;

public class MainActivity
extends Activity
implements DepositBTCFragment.OnFragmentInteractionListener,
           ActionBar.TabListener,
           TapTxnTask.TapTxnInitiator,
           Account.BalanceChangeListener {

    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    private NfcAdapter mNfcAdapter;
    private IntentFilter[] mNdefExchangeFilters;
    private PendingIntent mNfcPendingIntent;

    private Account account;

    private boolean mArmed = false;
    private ArmedFragment mArmFrag;
    private TextView tvBalance;

    CurrencyDAO currency;

    /**
     * For tapping, store the desired transaction object to be
     * referenced during background task execution
     */
    TapTxn outgoingTransaction = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        account = new Account();
        captureNFC();

    }

    private void captureNFC(){
        //Capture NFC interactions for this activity
        //TODO: make sure NFC is turned on or kill the APP with dialog
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        mNfcPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP
                | Intent.FLAG_ACTIVITY_CLEAR_TOP), 0);
        IntentFilter tapFilter = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            tapFilter.addDataScheme("http");
            tapFilter.addDataAuthority("tapnology.co",null);
            //tapFilter.addDataType("tapdat/performer");    /* Handles all MIME based dispatches.
              //                         You should specify only the ones that you need. */
        }
        catch (Exception  e) {
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
        if (!account.created()) {
            Intent intent = new Intent(this, AccountStartActivity.class);
            startActivityForResult(intent, AccountStartActivity.ACCOUNT_CREATION);
        }
        else {
            setupTabs();
        }
    }

    /**
     * Catch errors from the account creation screen and bail out if
     * they happen.
     */
    @Override
    protected void
    onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AccountStartActivity.ACCOUNT_CREATION) {
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }

    private void setupTabs(){
        //TODO: In the case where balance is zero open up a load phone fragment
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
            mNfcAdapter.enableForegroundDispatch(
                this,
                mNfcPendingIntent,
                mNdefExchangeFilters,
                null
            );
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if(mNfcAdapter != null) mNfcAdapter.disableForegroundDispatch(this);
    }

    //ARM SCREEN
    public void armOrSend(View v){
        mArmed=true;
        showArmedDialog();
        //TODO: make sure we unarm on resume
    }

    void showArmedDialog() {

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
        outgoingTransaction.setTxnAmount(account.getArmedAmount());
        outgoingTransaction.setCurrencyId(account.getActiveCurrency());
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
        outgoingTransaction.setTxnAmount(account.getArmedAmount());
        outgoingTransaction.setCurrencyId(account.getActiveCurrency());
        Log.d("TAP", "entered Transaction starting");
        new TapTxnTask().execute(this);
    }

    /**
     * When an NFC tag is detected, Android sends an Intent to any
     * application which has applied to receive them (see
     * AndroidManifest.xml).
     *
     * If we get an NDEF Intent, handle it as appropriate
     *
     * @param intent the intent sent to this activity
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Log.d("NFC", "NfcAdapter.ACTION_NDEF_DISCOVERED detected");
            final Vibrator vibe = (Vibrator)  getSystemService(Context.VIBRATOR_SERVICE);
            vibe.vibrate(100);
            long[] pattern = {0, 100, 50, 100, 100, 200};
            vibe.vibrate(pattern, -1);
            NdefMessage[] messages = null;
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                messages = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    messages[i] = (NdefMessage) rawMsgs[i];
                }
            }
            if (messages != null && messages[0] != null) {
                byte[] payload = messages[0].getRecords()[0].getPayload();
                // this assumes that we get back am SOH followed by host/code
                String result = new String(payload);
                Log.d("NFC", "record read: " + result);
                result = result.replaceAll("tapnology.co/tag/", "");
                result = result.replaceAll("\u0003", "");
                result = result.replaceAll("-", "");

                if (mArmed) {
                    Log.d("NFC", "Send transaction on tag " + result);
                    // Immediately disable from performing other txn
                    mArmed = false;
                    outgoingTransaction = new TapTxn();
                    outgoingTransaction.setTagID(result);
                    outgoingTransaction.setTxnAmount(account.getArmedAmount());
                    outgoingTransaction.setCurrencyId(account.getActiveCurrency());
                    new TapTxnTask().execute(this);
                } else {
                    //TODO: WHen not in armed mode, if intent is detected, change to send mode
                    Toast.makeText(getApplicationContext(), "Tap Not Ready.  Select Amount first.  Tag contains: " + result, Toast.LENGTH_SHORT).show();
                }
            }
            else {
                Log.d("NFC", "No messages found");
            }
        }
        else {
            Log.d("NFC", "Unexpected intent: " + intent.getAction());
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
        String mMessage = outgoingTransaction.getMessage();
        mArmFrag.updateWithResult(mMessage);
        outgoingTransaction = null;
        if (randomTransactionButton != null) {
            randomTransactionButton.setEnabled(true);
            randomTransactionButton = null;
        }
        // This opens the new Yapa page after a transaction
        Intent openYapa = new Intent(
            this,
            new YapaDisplay().getDisplayClass(t)
        );
        openYapa.putExtra(YapaDisplay.TRANSACTION_ID, t.getSlug());
        openYapa.putExtra(YapaDisplay.DELAY_TIME, 5);
        startActivity(openYapa);
    }

    // @TODO this should get a lot fancier, possibly with a screen
    // giving detailed guidance on how to load up on the desired
    // currency ... actually, the currency should have an instructions
    // field on the server that can contain written instructions to
    // the user in the event that this happens
    @Override
    public void tappedWrongCurrency(WrongCurrencyException wce) {
        int currencyId = wce.getCorrectCurrency();
        CurrencyDAO currency = new CurrencyDAO();
        currency.moveTo(currencyId);
        TapApplication.errorToUser(
            TapApplication.string(R.string.wrong_currency_0) +
            " " +
            currency.getName() +
            " " +
            TapApplication.string(R.string.wrong_currency_1)
        );
    }

    @Override
    public void onTapNetError(Throwable t) {
        TapApplication.handleFailures(this, t);
    }

    /**
     * Add a custom TextView to the title bar to display the balance
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        tvBalance = new TextView(this);
        tvBalance.setText(TapApplication.string(R.string.calculating));
        tvBalance.setTextColor(getResources().getColor(R.color.white));
        tvBalance.setPadding(0, 0, 10, 0);
        tvBalance.setTypeface(null, Typeface.BOLD);
        tvBalance.setTextSize(14);
        menu.add(0, -1, 1, R.string.tap).setActionView(tvBalance).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        Account.setBalanceChangeListener(this);
        return true;
    }

    /**
     * Update the balance on the title bar any time it changes
     */
    @Override
    public void onBalanceChanged(final BalanceList list) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    int currencyId = account.getActiveCurrency();
                    CurrencyDAO currency = new CurrencyDAO();
                    currency.moveTo(currencyId);
                    String value = currency.getSymbol() + list.get(currencyId);
                    tvBalance.setText(value);
                }
                catch (NullPointerException npe) {
                    // Can happen when things are starting up/shutting down,
                    // so ignore
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    public void onBackPressed(){
        CustomViewPager cvp = (CustomViewPager) findViewById(R.id.pager);
        if(cvp.getCurrentItem() == 2){
            finish();
        }
        else{
            cvp.setCurrentItem(2);
        }
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

    @Override
    public void onFragmentInteraction(Uri uri) {
        // we need this for fragments / menus
        //not sure what we have to do here if anything
    }

}
