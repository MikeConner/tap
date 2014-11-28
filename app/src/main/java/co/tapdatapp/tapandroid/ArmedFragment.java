package co.tapdatapp.tapandroid;



import android.app.DialogFragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import co.tapdatapp.tapandroid.service.TapCloud;


/**
 * A simple {@link Fragment} subclass.
 *
 */
public class ArmedFragment extends DialogFragment {
    private String mAuthToken;
    private float mAmount;

    public  void setValues (String message, String payload_url){

        TextView tv = (TextView) getView().findViewById(R.id.txtYap);
        ImageView iv = (ImageView) getView().findViewById(R.id.imageYapa);
        tv.setText(message);
        iv.setImageDrawable(TapCloud.LoadImageFromWebOperations(payload_url));
        iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

    }

    public ArmedFragment() {
        // Required empty public constructor
    }
    public ArmedFragment(String auth_token, float amount) {
            mAuthToken = auth_token;
            mAmount = amount;
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
        tv.setText( " About to TAP.  Tipping $" + mAmount);
    }


}
