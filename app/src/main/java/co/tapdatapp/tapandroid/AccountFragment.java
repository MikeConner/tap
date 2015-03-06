package co.tapdatapp.tapandroid;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import co.tapdatapp.tapandroid.currency.BalanceList;
import co.tapdatapp.tapandroid.currency.BalanceListAdapter;
import co.tapdatapp.tapandroid.currency.GetAllBalancesTask;
import co.tapdatapp.tapandroid.localdata.UserBalance;
import co.tapdatapp.tapandroid.user.Account;
import co.tapdatapp.tapandroid.voucher.DepositCodeFragment;

public class AccountFragment
extends Fragment
implements View.OnClickListener,
           AdapterView.OnItemClickListener,
           GetAllBalancesTask.Callback,
           DepositCodeFragment.Callback {

    private static final int SELECT_PICTURE = 1;
    private ListView balanceList;
    private Account account = new Account();
    private String selectedImagePath;
    private String filemanagerstring;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);
        ImageView profilePic = (ImageView) view.findViewById(R.id.profile_picture);
        profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newImage = new Intent();
                newImage.setType("image/*");
                newImage.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(newImage, "Select Image"),SELECT_PICTURE);
            }
        });

        return view;
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onResume(){
        super.onResume();

        View view = getView();

        TextView nickName = (TextView)view.findViewById(R.id.etNickName);
        nickName.setText(account.getNickname());

        TextView email = (TextView)view.findViewById(R.id.etEmail);
        String mEmailAddy = account.getEmail();
        if (mEmailAddy.isEmpty()) {
            email.setText("no@email.addy");
        }
        else {
            email.setText(mEmailAddy);
        }
        view.findViewById(R.id.btn_Load_Code).setOnClickListener(this);
        view.findViewById(R.id.btn_bitcoin_load).setOnClickListener(this);

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
            new UserBalance(),
            list
        );
        balanceList.setAdapter(adapter);
        // This will never be called with a null view
        //noinspection ConstantConditions
        getView().findViewById(R.id.balances_progress_bar).setVisibility(View.GONE);
        balanceList.setVisibility(View.VISIBLE);
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
        UserBalance currency = new UserBalance();
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == getActivity().RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                Uri selectedImageUri = data.getData();

                //OI FILE Manager
                filemanagerstring = selectedImageUri.getPath();

                //MEDIA GALLERY
                selectedImagePath = getPath(selectedImageUri);

                //DEBUG PURPOSE - you can delete this if you want
                if(selectedImagePath!=null)
                    System.out.println(selectedImagePath);
                else System.out.println("selectedImagePath is null");
                if(filemanagerstring!=null)
                    System.out.println(filemanagerstring);
                else System.out.println("filemanagerstring is null");

                //NOW WE HAVE OUR WANTED STRING
                if(selectedImagePath!=null)
                    System.out.println("selectedImagePath is the right one for you!");
                else
                    System.out.println("filemanagerstring is the right one for you!");
            }
        }
    }

    /**
     * This helps get the path for an image
     * @param uri
     * @return
     */
    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        if(cursor!=null)
        {
            //HERE YOU WILL GET A NULLPOINTER IF CURSOR IS NULL
            //THIS CAN BE, IF YOU USED OI FILE MANAGER FOR PICKING THE MEDIA
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        else return null;
    }
}
