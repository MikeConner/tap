package co.tapdatapp.tapandroid.yapa;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.content.ContentUris;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class YapaUrl extends Activity{

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_url);
        final ImageView imageView = (ImageView) findViewById(R.id.yapaUrl);
        final TextView urlSender = (TextView) findViewById(R.id.url_sender);
        final TextView urlDescription = (TextView) findViewById(R.id.url_description);
        final TextView urlDate = (TextView) findViewById(R.id.url_date);

        /**
         * Commented out Timestamps, was causing a crash
         */
        final Transaction transaction = new Transaction();
        ((TextView)urlSender.findViewById(R.id.url_sender)).setText(transaction.getNickname());
        ((TextView)urlDescription.findViewById(R.id.url_description)).setText(transaction.getDescription());
        //((TextView)urlDate.findViewById(R.id.url_date)).setText(transaction.getTimestamp().toString()
          //      + "  " + Integer.toString(transaction.getAmount()));
        /**
         * This opens up a webpage with the desired url
         */
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent goToURL = new Intent();
                goToURL.setAction(Intent.ACTION_VIEW);
                goToURL.addCategory(Intent.CATEGORY_BROWSABLE);
                goToURL.setData(Uri.parse(transaction.getYapa_url()));
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
