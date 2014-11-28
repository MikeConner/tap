package co.tapdatapp.tapandroid;



import android.app.DialogFragment;
import android.graphics.Point;
import android.os.Bundle;
import android.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import co.tapdatapp.tapandroid.service.TapCloud;
import co.tapdatapp.tapandroid.service.TapUser;


//TODO: put code in to run camera / get QR code and put text in to edittext

public class WithdrawFragment extends DialogFragment {


    public WithdrawFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
         View view =  inflater.inflate(R.layout.fragment_withdraw, container, false);

      /*  ((Button) view.findViewById(R.id.btnWithdraw)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView tv = (TextView) getView().findViewById(R.id.tvWithdrawStatus);
                tv.setText("Gettin' er done!");

                //TODO: Set code to withdraw
                //TODO: return to caller -> success or not, check balance / status of withdrawl!!!
                //TODO: THIS IS CRITICAL!
            }
        });
        */
        return view;
    }

    @Override
    public void onResume(){
        super.onResume();
   //     TextView tv = (TextView)  getView().findViewById(R.id.txtFrag);
   //     tv.setText( " About to TAP.  Tipping $" + mAmount);
        TapUser mTapUser = TapCloud.getTapUser(getActivity());
        String Outbound = mTapUser.getBTCoutbound();
        Button btnWithdraw = (Button) getView().findViewById(R.id.btnWithdraw);

        if (Outbound.equals("null") | Outbound.equals("")){
            //no addy here.. disable button
            //   btnWithdraw.setEnabled(false);
        }else
        {
            // btnWithdraw.setEnabled(true);
        }
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        //float densi = getActivity().getResources().getDisplayMetrics().density;
        getDialog().getWindow().setLayout(width, height);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME,android.R.style.Theme);


    }
    // setStyle(STYLE_NO_FRAME,android.R.style.Theme);

}
