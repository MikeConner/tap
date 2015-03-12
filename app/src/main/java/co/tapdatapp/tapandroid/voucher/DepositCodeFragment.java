/**
 * Fragment dialog that allows the user to enter a voucher number to
 * redeem
 */

package co.tapdatapp.tapandroid.voucher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.app.DialogFragment;
import android.widget.TextView;
import android.widget.Toast;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.UserFriendlyError;

public class DepositCodeFragment
extends DialogFragment
implements RedeemVoucherTask.Callback, View.OnClickListener {

    public interface Callback {
        void refreshBalanceList();
    }

    /**
     * The view contained within this Dialog
     */
    private View view;

    private Callback callback;

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

    public void setCallback(Callback c) {
        callback = c;
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
        callback.refreshBalanceList();
        dismiss();
    }

    /**
     * I'm pretty sure this is the right way to do this.
     * @param error
     */
    @Override
    public void onFailure(Throwable error){
        try{
            throw error;
        }
        catch(UserFriendlyError ufe){
            TapApplication.errorToUser(ufe);
        }
        catch(Throwable catchall) {
            TapApplication.unknownFailure(error);
        }
    }

}
