package co.tapdatapp.tapandroid;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import co.tapdatapp.tapandroid.service.TapCloud;
import co.tapdatapp.tapandroid.service.TapUser;


public class DepositFragment extends DialogFragment {

    private String mAuthToken;


    public  void setValues (String message, String payload_url){

      /*  TextView tv = (TextView) getView().findViewById(R.id.txtYap);
        ImageView iv = (ImageView) getView().findViewById(R.id.imageYapa);
        tv.setText(message);
        iv.setImageDrawable(TapCloud.LoadImageFromWebOperations(payload_url));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
*/
    }

    public DepositFragment() {
        // Required empty public constructor
    }
    public DepositFragment(String auth_token ) {
        mAuthToken = auth_token;

        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        float densi = getActivity().getResources().getDisplayMetrics().density;
     //   getDialog().getWindow().setLayout(width, height);
        return inflater.inflate(R.layout.fragment_deposit, container, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME,android.R.style.Theme);


    }
    @Override
    public void onResume(){
        super.onResume();
        ImageButton b = (ImageButton) getView().findViewById(R.id.check_mark);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //getActivity().getFragmentManager().beginTransaction().remove(this).commit();
//                FragmentTransaction ft = getFragmentManager().beginTransaction();
//                Fragment prev = getFragmentManager().findFragmentByTag("withdraw");
//                if (prev != null) {
//                    ft.remove(prev);
//                }
//                ft.addToBackStack(null);
            }
        });
        TextView btcInbound = (TextView) getView().findViewById(R.id.txtInboundAddy);
        TapUser mTapUser = TapCloud.getTapUser(getActivity());
        String mBTCaddy = mTapUser.getBTCinbound();
        ImageView iv = (ImageView) getView().findViewById(R.id.imgQRCODE);
        iv.setImageDrawable(TapCloud.LoadImageFromWebOperations(mTapUser.getQR()));
        btcInbound.setText(  mBTCaddy);


    }


}
