/**
 * Armed screen where the amount to pay is selected
 */

package co.tapdatapp.tapandroid.arm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.localdata.Denomination;
import co.tapdatapp.tapandroid.user.Account;

public class ArmFragment
extends Fragment
implements View.OnClickListener {

    private Account account = new Account();
    private TextView bankView;

    private final static LinearLayout.LayoutParams denominationLayoutParams;

    /**
     * Initialize layout parameters that will be reused for all
     * denomination images.
     */
    static {
        denominationLayoutParams = new LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        denominationLayoutParams.setMargins(70, 0, 70, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_arm, container, false);
        bankView = (TextView) view.findViewById(R.id.txtAmount);

        /**
         * Click on the Armed Amount to reset it.
         */
        bankView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bankView.performHapticFeedback(0);
                account.setArmedAmount(0);
                setAmount(account.getArmedAmount());
            }
        });
        return view;
    }

    /**
     * Set up the screen parameters when it's first created
     */
    @Override
    public void onResume() {
        super.onResume();
        setUpScreen();
    }

    /**
     * This method is called any time the visibility of this
     * fragment changes.
     */
    @Override
    public void setUserVisibleHint(boolean visible) {
        super.setUserVisibleHint(visible);
        if (visible) {
            setUpScreen();
        }
    }

    /**
     * This has to refresh the list of denominations each time the
     * fragment becomes visible again, that way it will show the
     * correct images if the user changes currency.
     */
    private void setUpScreen() {
        if (getView() != null) {
            setAmount(account.getArmedAmount());
            if (account.getActiveCurrency() == CurrencyDAO.CURRENCY_BITCOIN) {
                // Bitcoin configurations are handled specially
                updateBitcoinDenominations();
            } else {
                new SetupArmImagesTask().execute(this);
            }
        }
    }

    /**
     * Set the DISPLAY of the amount
     *
     * @param to int value of the amount
     */
    // @TODO include the currency symbol
    public void setAmount(int to) {
        bankView.setText(Integer.toString(to));
        // This will never be called when getView() is null
        //noinspection ConstantConditions
        getView().findViewById(R.id.btnArm).setEnabled(to != 0);
    }

    /**
     * Because bitcoin is a special case, we have to handle it
     * specially. The images are all included with the apk and not
     * downloaded, and the denominations are hardcoded in this
     * method.
     */
    private void updateBitcoinDenominations() {
        TextView viewAmount = (TextView)getView().findViewById(R.id.txtAmount);
        viewAmount.setBackground(getActivity().getResources().getDrawable(R.drawable.bitcoin_icon));
        viewAmount.getBackground().setAlpha(128);
        // This will never be called when getView() is null
        //noinspection ConstantConditions
        LinearLayout layout = (LinearLayout)getView().findViewById(R.id.currency_items);
        layout.removeAllViews();
        ImageView iv = new ImageView(getActivity());
        iv.setImageBitmap(
            scaleDenomination(
                BitmapFactory.decodeResource(
                    getActivity().getResources(),
                    R.drawable.generic_currency_1
                )
            )
        );
        iv.setTag(1);
        commonDenominationSetup(iv);
        layout.addView(iv);
        iv = new ImageView(getActivity());
        iv.setImageBitmap(
            scaleDenomination(
                BitmapFactory.decodeResource(
                    getActivity().getResources(),
                    R.drawable.generic_currency_5
                )
            )
        );
        iv.setTag(5);
        commonDenominationSetup(iv);
        layout.addView(iv);
        iv = new ImageView(getActivity());
        iv.setImageBitmap(
            scaleDenomination(
                BitmapFactory.decodeResource(
                    getActivity().getResources(),
                    R.drawable.generic_currency_10
                )
            )
        );
        iv.setTag(10);
        commonDenominationSetup(iv);
        layout.addView(iv);
    }

    /**
     * Called by SetupArmImagesTask when it is done fetching the
     * currency details and all images. The task is to display
     * them and set up the appropriate click handler.
     *
     * @param d Array of Denominations in ascending order
     * @param b Array of Bitmaps matching the Denominations
     */
    public void
    updateDenominations(Denomination[] d, Bitmap[] b, Bitmap logo) {
        TextView viewAmount = (TextView)getView().findViewById(R.id.txtAmount);
        viewAmount.setBackground(
            new BitmapDrawable(getActivity().getResources(), logo)
        );
        viewAmount.getBackground().setAlpha(128);
        // This will never be called when getView() is null
        //noinspection ConstantConditions
        LinearLayout layout = (LinearLayout)getView().findViewById(R.id.currency_items);
        layout.removeAllViews();
        for (int i = 0; i < d.length; i++) {
            ImageView iv = new ImageView(getActivity());
            iv.setImageBitmap(scaleDenomination(b[i]));
            commonDenominationSetup(iv);
            iv.setTag(d[i].getAmount());
            layout.addView(iv);
        }
    }

    /**
     * Receives clicks from all denomination images. The method then
     * determines the denomination amount from the tag that was
     * set when the ImageView was created.
     *
     * @param v The view that was clicked
     */
    @Override
    public void onClick(View v) {
        int amount = (Integer)v.getTag();
        account.setArmedAmount(account.getArmedAmount() + amount);
        setAmount(account.getArmedAmount());
    }

    /**
     * Common operations when configuring the ImageView whether it's
     * dynamically done or pulled from static data. margins, click
     * listener and any other formatting or common behavior.
     *
     * @param iv ImageView to set up to look correct as a denomination
     */
    private void commonDenominationSetup(ImageView iv) {
        iv.setOnClickListener(this);
        iv.setLayoutParams(denominationLayoutParams);
    }

    /**
     * Scale denomination Bitmap to the correct size
     *
     * @param in Raw image
     * @return image scaled correctly
     */
    public Bitmap scaleDenomination(Bitmap in) {
        return Bitmap.createScaledBitmap(in, 275, 100, true);
    }
}
