package co.tapdatapp.tapandroid;



import android.app.DialogFragment;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.app.Fragment;
import android.view.Display;
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

        new TapCloud.DownloadImageTask(iv)
                .execute(payload_url);

        //iv.setImageDrawable(TapCloud.LoadImageFromWebOperations(payload_url));

        //TODO: set options to do cropping in teh async background donwloader
        //iv.setScaleType(ImageView.ScaleType.CENTER_CROP);

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
        TextView tvAmount = (TextView)  getView().findViewById(R.id.txtArmedAmount);

        tv.setText( " TAP " );
        tvAmount.setText(  "$" + String.format("%d", (long)mAmount));
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        //float densi = getActivity().getResources().getDisplayMetrics().density;
        getDialog().getWindow().setLayout(width, height);
    }


}
