/**
 * Fragment dialog that allows the user to enter a voucher number to
 * redeem
 */

package co.tapdatapp.tapandroid;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.app.DialogFragment;
import android.widget.EditText;

import co.tapdatapp.tapandroid.voucher.RedeemVoucherTask;
import co.tapdatapp.tapandroid.voucher.VoucherRedeemResponse;

public class DepositCodeFragment
extends DialogFragment
implements RedeemVoucherTask.Callback, View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.fragment_deposit_code, container, false);

        v.findViewById(R.id.btnRedeem).setOnClickListener(this);

        return v;
    }

    @Override
    public void onClick(View v) {
        EditText etCode = (EditText) v.findViewById(R.id.etCode);
        new RedeemVoucherTask().execute(this, "bd0ccb78");

        // When button is clicked, call up to owning activity.
        //              ((FragmentDialog)getActivity()).showDialog();
    }

    @Override
    public void onComplete(VoucherRedeemResponse response){
        // Here you need to do whatever action is appropriate once the voucher has been
        // redeemed, such as show success dialog, or close the fragment, depending on the
        // UI requireents
    }

    @Override
    public void onFailure(Throwable error){
        // Here needs to handle the error, which could be to tell the user that their
        // network isn't available, or tell the that the voucher is invalid, or whatever
        // is appropriate based on the cause of the error
    }

}
