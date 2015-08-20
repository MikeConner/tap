/**
 * Wait for NFC contact and trigger a transaction
 */

package co.tapdatapp.tapandroid.arm;

import android.app.DialogFragment;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;



import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.Menu;
import android.widget.ImageView;

import co.tapdatapp.tapandroid.MainActivity;
import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.helpers.DevHelper;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.user.Account;


public class ArmedFragment extends DialogFragment implements View.OnClickListener{

    private EditText txtTagId;

    /**
     * Post-transaction, update the screen with the result
     *
     *  @param message textual "thank you" message
     */
    public void updateWithResult(String message) {
        // This will never be called with a null View
        //noinspection ConstantConditions

/*        ImageView taptag = (ImageView)getView().findViewById(R.id.taptagarmed);

        AnimatorSet tapgrow = (AnimatorSet) AnimatorInflater.loadAnimator(getView().getContext(), R.anim.taggrow);

        tapgrow.setTarget(taptag);
        tapgrow.start();
*/

        TextView tv = (TextView)getView().findViewById(R.id.txtYap);
        tv.setText(message);
    }

    /**
     * Mainly turn on dev buttons if this is a dev build, in addition
     * to the normal inflation.
     *
     * @param inflater per spec
     * @param container per spec
     * @param savedInstanceState per spec
     * @return View, per spec
     */
    @Override
    public View
    onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View armedFragment = inflater.inflate(R.layout.fragment_armed, container, false);
        if (DevHelper.isEnabled(R.string.CREATE_FAKE_DATA_ON_SERVER)) {
            Button b = (Button)armedFragment.findViewById(R.id.btnRandomTransaction);
            b.setVisibility(View.VISIBLE);
            b.setOnClickListener(this);
            b = (Button)armedFragment.findViewById(R.id.btnEnteredTransaction);
            b.setVisibility(View.VISIBLE);
            b.setOnClickListener(this);
            txtTagId = (EditText)armedFragment.findViewById(R.id.textTagId);
            txtTagId.setVisibility(View.VISIBLE);
            armedFragment.findViewById(R.id.imageYapa).setVisibility(View.GONE);
        }
        return armedFragment;
    }


    /**
     * Set the text amount
     */
    @Override
    public void onResume() {
        super.onResume();
        // This will never be called with a null View
        //noinspection ConstantConditions
        TextView tvAmount = (TextView)getView().findViewById(R.id.txtArmedAmount);
        CurrencyDAO currency = new CurrencyDAO();
        Account account = new Account();
        currency.moveTo(account.getActiveCurrency());
        tvAmount.setText(currency.getSymbol() + String.format("%d", account.getArmedAmount()));
        // What is the rest of this doing?
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        getDialog().getWindow().setLayout(width, height);
        ImageView armtap = (ImageView)getView().findViewById(R.id.imageYapa);
        ImageView taptag = (ImageView)getView().findViewById(R.id.taptagarmed);

        AnimatorSet armtapset = (AnimatorSet) AnimatorInflater.loadAnimator(getView().getContext(), R.anim.armtag);
        AnimatorSet tagtapset = (AnimatorSet) AnimatorInflater.loadAnimator(getView().getContext(), R.anim.armtag);

        armtapset.setTarget(armtap);
        armtapset.start();
        tagtapset.setTarget(taptag);
        tagtapset.start();
    }

    /**
     * The only buttons are development buttons, but both are dispatched
     * here
     *
     * @param v the button that was tapped
     */
    @Override
    public void onClick(View v) {
        v.setEnabled(false);
        switch (v.getId()) {
            case R.id.btnRandomTransaction :
                ((MainActivity)getActivity()).clickRandomTransaction(v);
                break;
            case R.id.btnEnteredTransaction :
                ((MainActivity)getActivity()).clickEnteredTransaction(txtTagId.getText().toString());
                break;
            default :
                throw new AssertionError("unknown button " + v.getId());
        }
    }


}
