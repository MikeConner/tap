package co.tapdatapp.tapandroid;

import android.app.DialogFragment;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import co.tapdatapp.tapandroid.helpers.DevHelper;
import co.tapdatapp.tapandroid.user.Account;


public class ArmedFragment extends DialogFragment implements View.OnClickListener{

    public  void setValues (String message, String payload_url){
        TextView tv = (TextView) getView().findViewById(R.id.txtYap);
        tv.setText(message);
    }

    public ArmedFragment() {
        // Required empty public constructor
    }

    @Override
    public View
    onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View armedFragment = inflater.inflate(R.layout.fragment_armed, container, false);
        if (DevHelper.isEnabled(R.string.CREATE_FAKE_DATA_ON_SERVER)) {
            Button b = (Button) armedFragment.findViewById(R.id.btnRandomTransaction);
            b.setVisibility(View.VISIBLE);
            b.setOnClickListener(this);
        }
        return armedFragment;
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

    @Override
    public void onClick(View v) {
        // Only a single button at this time
        ((MainActivity)getActivity()).clickRandomTransaction();
    }
}
