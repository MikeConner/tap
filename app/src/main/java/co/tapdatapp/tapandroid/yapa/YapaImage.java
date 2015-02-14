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

import co.tapdatapp.tapandroid.R;

/**
 * Created by Vince on 2/13/2015.
 */
public class YapaImage extends Activity {

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_image);
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause(){
        super.onPause();
    }

    /**
     * Doesn't work yet
     */
    public void goFullscreen(){

    }
}
