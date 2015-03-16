package co.tapdatapp.tapandroid.user;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

import co.tapdatapp.tapandroid.QRCode;
import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.currency.BalanceList;
import co.tapdatapp.tapandroid.currency.BalanceListAdapter;
import co.tapdatapp.tapandroid.currency.GetAllBalancesTask;
import co.tapdatapp.tapandroid.helpers.CustomViewPager;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.helpers.UserFriendlyError;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.voucher.DepositCodeFragment;

public class AccountFragment
extends Fragment
implements View.OnClickListener,
           AdapterView.OnItemClickListener,
           GetAllBalancesTask.Callback,
           DepositCodeFragment.Callback,
           SaveProfilePicTask.Callback {

    private static final int SELECT_PICTURE = 1;
    private ListView balanceList;
    private Account account = new Account();
    private ImageView profilePic;
    private TextView email;
    private TextView nickname;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResume(){
        super.onResume();

        View view = getView();

        CustomViewPager cvp = (CustomViewPager) getActivity().findViewById(R.id.pager);
        cvp.setPagingEnabled(true);
        nickname = (TextView)view.findViewById(R.id.etNickName);
        profilePic = (ImageView) view.findViewById(R.id.profile_picture);
        email = (TextView) view.findViewById(R.id.etEmail);
        nickname.setText(account.getNickname());
        profilePic.setOnClickListener(this);
        new SetProfilePicTask().execute();

        String mEmailAddy = account.getEmail();
        if (mEmailAddy.isEmpty()) {
            email.setText("no@email.addy");
        }
        else {
            email.setText(mEmailAddy);
        }
        view.findViewById(R.id.btn_Load_Code).setOnClickListener(this);
        view.findViewById(R.id.btn_bitcoin_load).setOnClickListener(this);
        view.findViewById(R.id.edit_email).setOnClickListener(this);
        view.findViewById(R.id.edit_nickname).setOnClickListener(this);
        nickname.setOnClickListener(this);
        email.setOnClickListener(this);

        balanceList = (ListView)view.findViewById(R.id.balances_list);
        balanceList.setOnItemClickListener(this);
        fillInList();
    }

    /**
     * This method is called any time the visibility of this
     * fragment changes.
     */
    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible) {
            // Race condition exists where this might be called
            // before the view is inflated
            if (getView() != null) {
                fillInList();
            }
        }
    }

    /**
     * Fill in the list with balances
     */
    private void fillInList() {
        // This will never be called with a null view
        //noinspection ConstantConditions
        getView().findViewById(R.id.balances_progress_bar).setVisibility(View.VISIBLE);
        balanceList.setVisibility(View.GONE);
        new GetAllBalancesTask().execute(this);
    }

    /**
     * Callback from GetAllBalancesTask once all balances are loaded.
     * Update the UI from here to actually display the balances.
     *
     * @param list List of Currency ID -> balance mappings
     */
    @Override
    public void onBalancesLoaded(BalanceList list) {
        BalanceListAdapter adapter = new BalanceListAdapter(
            getActivity(),
            new CurrencyDAO(),
            list
        );
        balanceList.setAdapter(adapter);
        // This will never be called with a null view
        //noinspection ConstantConditions
        getView().findViewById(R.id.balances_progress_bar).setVisibility(View.GONE);
        balanceList.setVisibility(View.VISIBLE);
    }

    /**
     * Called when there's an error loading balances
     * @param t
     */
    @Override
    public void onBalanceLoadFailure(Throwable t) {
        try {
            throw t;
        }
        catch (UserFriendlyError ufe) {
            TapApplication.errorToUser(ufe);
        }
        catch (Throwable catchall) {
            TapApplication.unknownFailure(t);
        }
    }

    /**
     * All button clicks go through this dispatcher
     *
     * @param v The button that was clicked
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_Load_Code :
                loadCode();
                break;
            case R.id.btn_bitcoin_load:
                openQR();
                break;
            case R.id.profile_picture:
                Intent newImage = new Intent();
                newImage.setType("image/*");
                newImage.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(newImage, "Select Image"), SELECT_PICTURE);
                break;
            case R.id.edit_nickname:
                changeNickname();
                break;
            case R.id.edit_email:
                changeEmail();
                break;
            case R.id.etNickName:
                changeNickname();
                break;
            case R.id.etEmail:
                changeEmail();
                break;
            default :
                throw new AssertionError("Unknown button " + v.getId());
        }
    }

    /**
     * Start the dialog fragment to redeem a voucher
     */
    private void loadCode() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment prev = getFragmentManager().findFragmentByTag("tapcode");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        // Create and show the dialog.
        DepositCodeFragment fragment = new DepositCodeFragment();
        fragment.setCallback(this);
        fragment.show(ft, "tapcode");
    }

    /**
     *
     */
    public void openQR(){
        Intent loadQR = new Intent(getActivity(), QRCode.class);
        startActivity(loadQR);
    }

    /**
     * Opens a dialog to change the nickname
     */
    public void changeNickname(){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle("Edit Nickname");
        alert.setMessage("Enter a new nickname:");

        final EditText input = new EditText(getActivity());
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                CharSequence text = input.getText();
                String newName = text.toString();
                account.setNickname(newName);
                nickname.setText(account.getNickname());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    /**
     * Opens a dialog to change the e-mail
     */
    public void changeEmail(){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle("Edit E-mail");
        alert.setMessage("Enter a new e-mail address:");

        final EditText input = new EditText(getActivity());
        alert.setView(input);

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                CharSequence text = input.getText();
                String newEmail = text.toString();
                account.setEmail(newEmail);
                email.setText(account.getEmail());
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    /**
     * Taps on balances call this. All it does is set the active
     * currency on the account.
     *
     * @param parent per spec
     * @param view per spec
     * @param position Item # in the list view
     * @param id The currency ID
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (id > Integer.MAX_VALUE) {
            throw new AssertionError("Currency ID exceeds int size");
        }
        account.setActiveCurrency((int) id);
        CurrencyDAO currency = new CurrencyDAO();
        currency.moveTo((int)id);
        Toast toast = Toast.makeText(
            getActivity(),
            TapApplication.string(R.string.currency_changed) + " " +
                currency.getName(),
            Toast.LENGTH_LONG
        );
        toast.show();
    }

    /**
     * Called by the voucher redemption dialog to tell this view that
     * the list of balances has changed.
     */
    @Override
    public void refreshBalanceList() {
        fillInList();
    }

    /**
     * Receives results from any startActivityForResult() calls, so
     * far only receives results for the user selecting a profile pic.
     *
     * This actually only works if you select from the gallery.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                InputStream imageStream;
                try {
                    imageStream = getActivity().getContentResolver().openInputStream(data.getData());
                }
                catch (FileNotFoundException fnfe) {
                    TapApplication.errorToUser("The selected file is unavailable");
                    return;
                }
                new SaveProfilePicTask().execute(this, imageStream);
            }
            else {
                throw new AssertionError("Unkown request code: " + requestCode);
            }
        }
        else {
            throw new AssertionError("Unknown activity result code: " + resultCode);
        }
    }

    /**
     * Called when the profile picture has been successfully saved
     *
     * @param id The ID/URL of the profile picture
     */
    @Override
    public void onProfilePicSaved(String id) {
        new SetProfilePicTask().execute();
    }

    /**
     * Called if unable to save the profile picture
     *
     * @param t The exception that caused the failure
     */
    @Override
    public void onProfilePicSaveFailed(Throwable t) {
        TapApplication.unknownFailure(t);
    }

    /**
     * Load the profile image onto the view, fetching from the web if
     * necessary.
     */
    private class SetProfilePicTask
    extends AsyncTask<Void, Void, Exception> {

        private Bitmap thumbnail;

        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Account a = new Account();
                if (a.getProfilePicThumbUrl() != null) {
                    thumbnail = TapBitmap.fetchFromCacheOrWeb(
                        new Account().getProfilePicThumbUrl()
                    );
                }
                return null;
            }
            catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onPostExecute(Exception e) {
            if (e != null) {
                TapApplication.unknownFailure(e);
            }
            else {
                profilePic.setImageBitmap(thumbnail);
            }
        }
    }
}
