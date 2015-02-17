package co.tapdatapp.tapandroid;

import android.app.DialogFragment;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import co.tapdatapp.tapandroid.user.Account;


public class ArmedFragment extends DialogFragment {

    public  void setValues (String message, String payload_url){

        TextView tv = (TextView) getView().findViewById(R.id.txtYap);
        tv.setText(message);

    }

    public ArmedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);

        return inflater.inflate(R.layout.fragment_armed, container, false);


    }
    @Override
    public void onResume(){
        super.onResume();
        TextView tv = (TextView)  getView().findViewById(R.id.txtFrag);
        TextView tvAmount = (TextView)  getView().findViewById(R.id.txtArmedAmount);

        tv.setText( " TAP " );
        tvAmount.setText(  "$" + String.format("%d", new Account().getArmedAmount()));
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        //float densi = getActivity().getResources().getDisplayMetrics().density;
        getDialog().getWindow().setLayout(width, height);
    }


}
