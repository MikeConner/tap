package co.tapdatapp.tapandroid.yapa;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.content.ContentUris;

import co.tapdatapp.tapandroid.R;

public class YapaUrl extends Activity{

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_url);
        final ImageView imageView = (ImageView) findViewById(R.id.yapaUrl);

        /**
         * This opens up a webpage with the desired url
         */
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent goToURL = new Intent();
                goToURL.setAction(Intent.ACTION_VIEW);
                goToURL.addCategory(Intent.CATEGORY_BROWSABLE);
                goToURL.setData(Uri.parse("http://google.com"));
                startActivity(goToURL);
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
