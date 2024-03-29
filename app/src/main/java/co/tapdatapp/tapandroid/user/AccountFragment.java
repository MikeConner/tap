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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.currency.BalanceList;
import co.tapdatapp.tapandroid.currency.BalanceListAdapter;
import co.tapdatapp.tapandroid.currency.GetAllBalancesTask;
import co.tapdatapp.tapandroid.helpers.CustomViewPager;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
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
    private CustomViewPager cvp;
    private boolean editMode = false;
    private Button editButton;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_account, container, false);
    }

    @Override
    public void onResume(){
        super.onResume();
        View view = getView();
        if (view == null) {
            Log.e("IGNORED", "onResume() getView() returned null", new Exception());
            return;
        }
        cvp = (CustomViewPager) getActivity().findViewById(R.id.pager);
        cvp.setPagingEnabled(true);
        nickname = (TextView)view.findViewById(R.id.etNickName);
        profilePic = (ImageView)view.findViewById(R.id.profile_picture);
        email = (TextView)view.findViewById(R.id.etEmail);
        editButton = (Button) view.findViewById(R.id.btn_edit_profile);
        nickname.setText(account.getNickname());
        profilePic.setOnClickListener(null);

        String mEmailAddy = account.getEmail();
        if (mEmailAddy.isEmpty()) {
            email.setText(R.string.default_email);
        } else {
            email.setText(mEmailAddy);
        }
        view.findViewById(R.id.btn_Load_Code).setOnClickListener(this);
        view.findViewById(R.id.btn_bitcoin_load).setOnClickListener(this);
        view.findViewById(R.id.btn_manage_tags).setOnClickListener(this);
        editButton.setOnClickListener(this);
        nickname.setOnClickListener(null);
        email.setOnClickListener(null);
        view.findViewById(R.id.account_layout).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                cvp.setPagingEnabled(true);
                return false;
            }
        });

        balanceList = (ListView) view.findViewById(R.id.balances_list);
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
            new SetProfilePicTask().execute();
            // Race condition exists where this might be called
            // before the view is inflated
            if (getView() != null) {
                fillInList();
            }
        }
        else {
            try {
                balanceList.setAdapter(null);
                profilePic.setImageDrawable(null);
            }
            catch (NullPointerException npe) {
                // Ignore, happens when the view is first started
            }
        }
    }

    /**
     * Fill in the list with balances
     */
    private void fillInList() {
        try {
            //noinspection ConstantConditions
            getView().findViewById(R.id.balances_progress_bar).setVisibility(View.VISIBLE);
            balanceList.setVisibility(View.GONE);
            new GetAllBalancesTask().execute(this);
        }
        catch (NullPointerException npe) {
            // This can happen if the user navigates away from the
            // Activity faster than the background task can finish,
            // and can be ignored
        }
    }

    /**
     * Callback from GetAllBalancesTask once all balances are loaded.
     * Update the UI from here to actually display the balances.
     *
     * @param list List of Currency ID -> balance mappings
     */
    @Override
    public void onBalancesLoaded(BalanceList list) {
        try {
            BalanceListAdapter adapter = new BalanceListAdapter(
                getActivity(),
                new CurrencyDAO(),
                list
            );
            balanceList.setAdapter(adapter);
            //noinspection ConstantConditions
            getView().findViewById(R.id.balances_progress_bar).setVisibility(View.GONE);
            balanceList.setVisibility(View.VISIBLE);
            cvp.setPagingEnabled(true);
        }
        catch (NullPointerException npe) {
            // This can happen if the user navigates away from the
            // Activity faster than the background task can finish,
            // and can be ignored
        }
    }

    @Override
    public void onBalanceLoadFailed(Throwable t) {
        TapApplication.handleFailures(getActivity(), t);
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
            case R.id.etNickName:
                changeNickname();
                break;
            case R.id.etEmail:
                changeEmail();
                break;
            case R.id.btn_edit_profile:
                makeEditable();
                break;
            case R.id.btn_manage_tags:
                goToTags();
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

        alert.setTitle(R.string.edit_nickname);
        alert.setMessage(R.string.enter_new_nickname);

        final EditText input = new EditText(getActivity());
        alert.setView(input);

        alert.setPositiveButton(R.string.positive_alert, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                CharSequence text = input.getText();
                String newName = text.toString();
                account.setNickname(newName);
                nickname.setText(account.getNickname());
            }
        });

        alert.setNegativeButton(R.string.negative_alert, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    /**
     * This toggles whether the nickname, e-mail, and profile picture are editable
     */
    public void makeEditable(){
        ImageView editPic = (ImageView) getActivity().findViewById(R.id.edit_indicator);

        if(!editMode){
            editPic.setVisibility(View.VISIBLE);
            nickname.setOnClickListener(this);
            profilePic.setOnClickListener(this);
            email.setOnClickListener(this);
            editMode = true;
            editButton.setText(R.string.done_editing);
        }
        else{
            editPic.setVisibility(View.GONE);
            nickname.setOnClickListener(null);
            profilePic.setOnClickListener(null);
            email.setOnClickListener(null);
            editMode = false;
            editButton.setText(R.string.edit_profile);
        }

    }


    /**
     * Opens a dialog to change the e-mail
     */
    public void changeEmail(){
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

        alert.setTitle(R.string.edit_email);
        alert.setMessage(R.string.enter_new_email);

        final EditText input = new EditText(getActivity());
        alert.setView(input);

        alert.setPositiveButton(R.string.positive_alert, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                CharSequence text = input.getText();
                String newEmail = text.toString();
                account.setEmail(newEmail);
                email.setText(account.getEmail());
            }
        });

        alert.setNegativeButton(R.string.negative_alert, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();
    }

    /**
     * Goes to the tags screen
     */
    public void goToTags(){
        cvp.setCurrentItem(0);
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
                    TapApplication.errorToUser(TapApplication.string(R.string.file_access_problem));
                    return;
                }
                new SaveProfilePicTask().execute(this, imageStream);
            }
            else {
                throw new AssertionError("Unkown request code: " + requestCode);
            }
        }
        else {
            TapApplication.errorToUser(TapApplication.string(R.string.no_image_selected));
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

    @Override
    public void onProfileSaveFailure(Throwable t) {
        TapApplication.handleFailures(getActivity(), t);
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
                TapApplication.handleFailures(getActivity(), e);
            }
            else {
                profilePic.setImageBitmap(thumbnail);
            }
        }
    }
}
