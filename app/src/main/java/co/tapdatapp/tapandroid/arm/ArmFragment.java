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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;


import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.helpers.CustomViewPager;
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
    private CustomViewPager cvp;

    private int maxIndex;
    private int curIndex = 0;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_arm, container, false);
        bankView = (TextView) view.findViewById(R.id.txtAmount);
        vf = (ViewFlipper) view.findViewById(R.id.currency_items);
        cvp = (CustomViewPager) getActivity().findViewById(R.id.pager);
        LinearLayout swipeLayout = (LinearLayout) view.findViewById(R.id.clickable_area);
        LinearLayout scrollLayout = (LinearLayout) view.findViewById(R.id.scrollable_area);
        cvp.setPagingEnabled(false);
        /**
         * This re-enables scrolling between pages
         */
        swipeLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                cvp.setPagingEnabled(true);
                return false;
            }
        });

        /**
         * This disables scrolling between pages while changing denominations
         */
        scrollLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                cvp.setPagingEnabled(false);
                return false;
            }
        });

        /**
         * Click on the Armed Amount to reset it.
         */
        bankView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bankView.performHapticFeedback(0);
                bankView.setTextSize(140);
                account.setArmedAmount(0);
                setAmount(account.getArmedAmount());
            }
        });

        /**
         * This is the fling event for adding money to the bank
         *
         * To change denominations, you need to initiate a touch event in the denomination area.
         */
        gesture = new GestureDetector(getActivity(),
                new GestureDetector.SimpleOnGestureListener() {
                    /**
                     * This is basically a constructor. If it doesn't return true, the gesture detector will exit,
                     * but it shouldn't do anything.
                     * @param e is the variable for the motion event.
                     * @return boolean values that tell whether or not an event happened.
                     */
                    @Override
                    public boolean onDown(MotionEvent e) {
                        return true;
                    }

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                                           float velocityY) {
                        final int SWIPE_MIN_DISTANCE = 250;
                        final int SWIPE_MAX_OFF_PATH = 250;
                        final int SWIPE_THRESHOLD_VELOCITY = 200;
                        try {
                            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH){
                                account.setArmedAmount(account.getArmedAmount() + amount);
                                setAmount(account.getArmedAmount());
                            }
                            if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                if(curIndex < maxIndex) {
                                    //Animation
                                    vf.setInAnimation(getActivity(), R.anim.in_from_right);
                                    vf.setOutAnimation(getActivity(), R.anim.out_to_left);

                                    vf.showNext();
                                    curIndex++;
                                }
                            } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                                    && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                                if(curIndex > 0) {
                                    //Animation
                                    vf.setInAnimation(getActivity(), R.anim.in_from_left);
                                    vf.setOutAnimation(getActivity(), R.anim.out_to_right);

                                    vf.showPrevious();
                                    curIndex--;
                                }
                            }
                        } catch (Exception e) {

                            //  I don't think anything needs to go in here. If the event fails, it's not a fatal error.

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
            curIndex = 0;
            cvp.setPagingEnabled(false);
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
        maxIndex = 2;
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
        maxIndex = d.length-1;
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
     * @param view this is the view of the current denomination
     * @param event the motion event
     * @return returns whether there was a motion event detected.
     */
    @Override
    public boolean onTouch(View view, MotionEvent event){
        amount = (Integer) view.getTag();
        cvp.setPagingEnabled(false);
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
