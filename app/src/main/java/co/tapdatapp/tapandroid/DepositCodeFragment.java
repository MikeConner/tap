package co.tapdatapp.tapandroid;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.app.DialogFragment;
import android.graphics.Point;
import android.view.Display;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import co.tapdatapp.tapandroid.voucher.RedeemVoucherTask;
import co.tapdatapp.tapandroid.voucher.VoucherRedeemResponse;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link DepositCodeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link DepositCodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DepositCodeFragment extends DialogFragment implements RedeemVoucherTask.Callback {


    private EditText etCode;
    private Button redeem_button;

    private OnFragmentInteractionListener mListener;

    public static DepositCodeFragment newInstance() {
        DepositCodeFragment fragment = new DepositCodeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public DepositCodeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View v =  inflater.inflate(R.layout.fragment_deposit_code, container, false);

        redeem_button = (Button) v.findViewById(R.id.btnRedeem);
        redeem_button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                etCode = (EditText) v.findViewById(R.id.etCode);
                new RedeemVoucherTask().execute(this, "bd0ccb78");

                // When button is clicked, call up to owning activity.
  //              ((FragmentDialog)getActivity()).showDialog();
            }
        });


        return v;
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

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
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
