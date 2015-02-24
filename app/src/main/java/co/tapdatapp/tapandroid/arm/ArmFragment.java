package co.tapdatapp.tapandroid.arm;

import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnTouchListener;

import java.util.ArrayList;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.user.Account;

public class ArmFragment extends Fragment {

    Account account = new Account();
    private static final int SWIPE_MIN_DISTANCE = 5;
    private static final int SWIPE_THRESHOLD_VELOCITY = 300;
    private GestureDetector mGestureDetector;
    private int mActiveFeature = 0;

    public int bankAmt = account.getArmedAmount();



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_arm, container, false);
        final LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.currency_items);
        final HorizontalScrollView scrollView = (HorizontalScrollView) view.findViewById(R.id.currency_scroll);
        mGestureDetector  = new GestureDetector(new MyGestureDetector());
        final ImageView oneView = (ImageView) view.findViewById(R.id.currency_1);
        final ImageView fiveView = (ImageView) view.findViewById(R.id.currency_5);
        final ImageView tenView = (ImageView) view.findViewById(R.id.currency_10);
        final TextView bankView = (TextView) view.findViewById(R.id.txtAmount);

        linearLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //If the user swipes
                if (mGestureDetector.onTouchEvent(event)) {
                    return true;
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    int scrollX = linearLayout.getScrollX();
                    int featureWidth = scrollView.getMeasuredWidth();
                    mActiveFeature = ((scrollX + (featureWidth / 2)) / featureWidth);
                    int scrollTo = mActiveFeature * featureWidth;
                    scrollView.smoothScrollTo(scrollTo, 0);
                    return true;
                } else {
                    return false;
                }
            }
        });
        /**
         * Adding :
         * account.getActiveCurrency() +
         * to the bankView.setText() line completely messes up the math, I'm not sure why.
         */
        oneView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bankAmt = bankAmt + 1;
                account.setArmedAmount(bankAmt);
                bankView.setText(String.valueOf(account.getArmedAmount()));
            }
        });

        fiveView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bankAmt = bankAmt + 5;
                account.setArmedAmount(bankAmt);
                bankView.setText(String.valueOf(account.getArmedAmount()));
            }
        });

        tenView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bankAmt = bankAmt + 10;
                account.setArmedAmount(bankAmt);
                bankView.setText(String.valueOf(account.getArmedAmount()));
            }
        });

        /**
         * Click on the Armed Amount to reset it.
         */
        bankView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bankAmt = 0;
                account.setArmedAmount(bankAmt);
                bankView.setText(String.valueOf(account.getArmedAmount()));
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
       account.setArmedAmount(account.getArmedAmount());
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        final LinearLayout linearLayout = (LinearLayout) getActivity().findViewById(R.id.currency_items);
        final HorizontalScrollView scrollView = (HorizontalScrollView) getActivity().findViewById(R.id.currency_scroll);
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                //right to left
                if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    int featureWidth = linearLayout.getMeasuredWidth();
                    mActiveFeature = (mActiveFeature < (3 - 1))? mActiveFeature + 1:3 -1;
                    scrollView.smoothScrollTo(mActiveFeature * featureWidth, 0);
                    return true;
                }
                //left to right
                else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    int featureWidth = linearLayout.getMeasuredWidth();
                    mActiveFeature = (mActiveFeature > 0)? mActiveFeature - 1:0;
                    scrollView.smoothScrollTo(mActiveFeature * featureWidth, 0);
                    return true;
                }
            } catch (Exception e) {
                TapApplication.unknownFailure(e);
            }
            return false;
        }
    }

    protected class myDragEventListener implements Button.OnDragListener {

        // This is the method that the system calls when it dispatches a drag event to the
        // listener.
        public boolean onDrag(View v, DragEvent event) {

            // Defines a variable to store the action type for the incoming event
            final int action = event.getAction();

            // Handles each of the expected events
            switch(action) {

                case DragEvent.ACTION_DRAG_STARTED:

                    /* Determines if this View can accept the dragged data
                    if (event.getClipDescription().hasMimeType(ClipDescriptiom.MIMETYPE_TEXT_PLAIN)) {

                        // As an example of what your application might do,
                        // applies a blue color tint to the View to indicate that it can accept
                        // data.
                        v.setColorFilter(Color.BLUE);

                        // Invalidate the view to force a redraw in the new tint
                        v.invalidate();

                        // returns true to indicate that the View can accept the dragged data.
                        return true;

                    }

                    // Returns false. During the current drag and drop operation, this View will
                    // not receive events again until ACTION_DRAG_ENDED is sent.
                    return false;
                    */

                case DragEvent.ACTION_DRAG_ENTERED:

                    // Applies a green tint to the View. Return true; the return value is ignored.

                    v.setBackgroundColor(Color.GREEN);

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate();

                    return true;

                case DragEvent.ACTION_DRAG_LOCATION:

                    // Ignore the event
                    return true;

                case DragEvent.ACTION_DRAG_EXITED:

                    // Re-sets the color tint to blue. Returns true; the return value is ignored.
                    v.setBackgroundColor(Color.BLUE);

                    // Invalidate the view to force a redraw in the new tint
                    v.invalidate();

                    return true;

                case DragEvent.ACTION_DROP:

                    // Gets the item containing the dragged data
                    //                  ClipData.Item item = event.getClipData().getItemAt(0);

                    // Gets the text data from the item.
                    //                dragData = item.getText();

                    // Displays a message containing the dragged data.
                    Toast.makeText(getView().getContext(), event.getClipData().toString(), Toast.LENGTH_LONG);

                    // Turns off any color tints
                    v.setBackgroundColor(Color.RED);

                    // Invalidates the view to force a redraw
                    v.invalidate();

                    // Returns true. DragEvent.getResult() will return true.
                    return true;

                case DragEvent.ACTION_DRAG_ENDED:

                    // Turns off any color tinting
//                    v.clearColorFilter();

                    // Invalidates the view to force a redraw
                    v.invalidate();

                    // Does a getResult(), and displays what happened.
                    if (event.getResult()) {
                        Toast.makeText(getView().getContext(), "The drop was handled.", Toast.LENGTH_LONG);

                    } else {
                        Toast.makeText(getView().getContext(), "The drop didn't work.", Toast.LENGTH_LONG);

                    }

                    // returns true; the value is ignored.
                    return true;

                // An unknown action type was received.
                default:
                    Log.e("DragDrop Example", "Unknown action type received by OnDragListener.");
                    break;
            }

            return false;
        }
    }


}
