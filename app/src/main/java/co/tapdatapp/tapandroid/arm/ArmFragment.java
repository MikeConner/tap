/**
 * Armed screen where the amount to pay is selected
 */

package co.tapdatapp.tapandroid.arm;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ViewFlipper;


import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.localdata.Denomination;
import co.tapdatapp.tapandroid.user.Account;

public class ArmFragment
extends Fragment
implements View.OnTouchListener {

    private Account account = new Account();
    private TextView bankView;
    private int amount;
    private GestureDetector gesture;
    private ViewFlipper vf;
    private TextView prevDem;
    private TextView nextDem;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_arm, container, false);
        bankView = (TextView) view.findViewById(R.id.txtAmount);
        vf = (ViewFlipper) view.findViewById(R.id.currency_items);
        prevDem = (TextView) view.findViewById(R.id.left_button);
        nextDem = (TextView) view.findViewById(R.id.right_button);
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

        /**
         * Button to show previous denomination
         */
        prevDem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Animation
                vf.setInAnimation(getActivity(),R.animator.in_from_left);
                vf.setOutAnimation(getActivity(), R.animator.out_to_right);

                vf.showPrevious();

            }
        });

        /**
         * Button to show next denomination
         */
        nextDem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Animation
                vf.setInAnimation(getActivity(),R.animator.in_from_right);
                vf.setOutAnimation(getActivity(), R.animator.out_to_left);

                vf.showNext();
            }
        });

        /**
         * This is the fling event for adding money to the bank
         */
        gesture = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {
                    /**
                     * This is basically a constructor. If it doesn't return true, the gesture detector will exit,
                     * but it shouldn't do anything.
                     * @param e
                     * @return
                     */
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                           float velocityY) {
                        final int SWIPE_MIN_DISTANCE = 200;
                        final int SWIPE_MAX_OFF_PATH = 250;
                        final int SWIPE_THRESHOLD_VELOCITY = 200;
                        try {
                            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH){
                                account.setArmedAmount(account.getArmedAmount() + amount);
                                setAmount(account.getArmedAmount());
                            }
                            else if (Math.abs(e2.getY() - e1.getY()) > SWIPE_MAX_OFF_PATH){
                            }
                            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            }
                        } catch (Exception e) {
                            // nothing
                        }
                        return super.onFling(e1, e2, velocityX, velocityY);
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
     * Added a statement to shrink the text size to fit in the view
     *
     * @param to int value of the amount
     */
    // @TODO include the currency symbol
    public void setAmount(int to) {

        if(to>99){
            bankView.setTextSize(100);
        }

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
        ViewFlipper layout = (ViewFlipper)getView().findViewById(R.id.currency_items);
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
        ViewFlipper layout = (ViewFlipper)getView().findViewById(R.id.currency_items);
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
     * This starts the event that causes up-swipes to deposit denominations into the bank.
     *
     * @param view
     * @param event
     * @return
     */
    @Override
    public boolean onTouch(View view, MotionEvent event){
        amount = (Integer)view.getTag();
        return gesture.onTouchEvent(event);
    }

    /**
     * Common operations when configuring the ImageView whether it's
     * dynamically done or pulled from static data. margins, click
     * listener and any other formatting or common behavior.
     *
     * @param iv ImageView to set up to look correct as a denomination
     */
    private void commonDenominationSetup(ImageView iv) {
        iv.setOnTouchListener(this);
    }

    /**
     * Scale denomination Bitmap to the correct size
     *
     * Made this 3 times bigger, seems to make it it's original size.
     *
     * @param in Raw image
     * @return image scaled correctly
     */
    public Bitmap scaleDenomination(Bitmap in) {
        return Bitmap.createScaledBitmap(in, 825, 300, true);
    }

}
