package co.tapdatapp.tapandroid;

import android.app.Activity;
import android.content.ClipData;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.DragEvent;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ArmFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ArmFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class ArmFragment extends Fragment {


    private OnFragmentInteractionListener mListener;

    // TODO: Rename and change types and number of parameters
    public static ArmFragment newInstance() {
        ArmFragment fragment = new ArmFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
        return fragment;
    }
    public ArmFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment


        return inflater.inflate(R.layout.fragment_arm, container, false);

    }

    @Override
    public void onResume() {
        super.onResume();
        getView().findViewById(R.id.btnOne).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                ClipData data = ClipData.newPlainText("","");
                View.DragShadowBuilder shadow = new View.DragShadowBuilder(getView().findViewById(R.id.btnOne));
                view.startDrag(data,shadow,null,0);
                return false;
            }
        });
        getView().findViewById(R.id.btnOne).setOnDragListener(new myDragEventListener());



    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
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
