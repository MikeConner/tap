package co.tapdatapp.tapandroid.yapa;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class YapaImage extends Activity {

    private boolean isImageFitToScreen = false;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_image);
        final ImageView imageView = (ImageView) findViewById(R.id.yapaImage);
        final TextView imageSender = (TextView) findViewById(R.id.image_sender);
        final TextView imageDescription = (TextView) findViewById(R.id.image_description);
        final TextView imageDate = (TextView) findViewById(R.id.image_date);

        /**
         * Commented out Timestamps, was causing a crash
         */
        Transaction transaction = new Transaction();
        ((TextView)imageSender.findViewById(R.id.image_sender)).setText(transaction.getNickname());
        ((TextView)imageDescription.findViewById(R.id.image_description)).setText(transaction.getDescription());
        //((TextView)imageDate.findViewById(R.id.image_date)).setText(transaction.getTimestamp().toString()
          //      + "  " + Integer.toString(transaction.getAmount()));


        /**
         * This makes clicking on the image turn it into a fullscreen view.
         */
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isImageFitToScreen) {
                    isImageFitToScreen=false;
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                    imageView.setAdjustViewBounds(true);
                }else{
                    isImageFitToScreen=true;
                    imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                }
            }
        });


    }

    public void onResume() {
        super.onResume();
    }

    public void onPause(){
        super.onPause();
    }
}
