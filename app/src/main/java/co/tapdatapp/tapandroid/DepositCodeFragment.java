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
import android.widget.TextView;
import android.widget.Toast;

import co.tapdatapp.tapandroid.voucher.RedeemVoucherTask;
import co.tapdatapp.tapandroid.voucher.VoucherRedeemResponse;

public class DepositCodeFragment
extends DialogFragment
implements RedeemVoucherTask.Callback, View.OnClickListener {

    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        view = inflater.inflate(R.layout.fragment_deposit_code, container, false);
        view.findViewById(R.id.btnRedeem).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        TextView tv = (TextView)view.findViewById(R.id.etCode);
        CharSequence text = tv.getText();
        String code = text.toString();
        new RedeemVoucherTask().execute(this, code);
    }

    @Override
    public void onComplete(VoucherRedeemResponse response){
        Toast toast = Toast.makeText(
            getActivity().getApplicationContext(),
            TapApplication.string(R.string.successful_redeem) +
                " " + response.getAmountRedeemed(),
            Toast.LENGTH_LONG
        );
        toast.show();
        dismiss();
    }

    @Override
    public void onFailure(Throwable error){
        TapApplication.unknownFailure(error);
    }

}
