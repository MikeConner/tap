package co.tapdatapp.tapandroid.yapa;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import co.tapdatapp.tapandroid.MainActivity;
import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class YapaVideo extends Activity{

    private boolean forceReturnToArmScreen = false;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_url);

        Bundle extras = getIntent().getExtras();
        final String transactionId = extras.getString(YapaDisplay.TRANSACTION_ID);
        final Transaction transaction = new Transaction();
        transaction.moveToSlug(transactionId);

        final ImageView imageView = (ImageView) findViewById(R.id.yapaUrl);
        final TextView urlSender = (TextView) findViewById(R.id.url_sender);
        final TextView urlDescription = (TextView) findViewById(R.id.url_description);
        final TextView urlDate = (TextView) findViewById(R.id.url_date);

        urlSender.setText(transaction.getNickname());
        urlDescription.setText(transaction.getDescription());
        urlDate.setText(transaction.getTimestamp().toString() + "  " + Integer.toString(transaction.getAmount()));

        /**
         * This needs to be tested and probably changed in the future.
         */
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                Intent openVideo = new Intent(Intent.ACTION_VIEW, Uri.parse(transaction.getYapa_url()));
                openVideo.setAction(Intent.ACTION_VIEW);
                openVideo.addCategory(Intent.CATEGORY_BROWSABLE);
                openVideo.setData(Uri.parse(transaction.getYapa_url()));
                startActivity(openVideo);
            }
        });

        int showTime = extras.getInt(YapaDisplay.DELAY_TIME, -1);
        if (showTime != -1) {
            /**
             * This makes the view disappear after the specified wait
             */
            forceReturnToArmScreen = true;
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    onBackPressed();
                }
            };
            YapaDisplay.delayWorker.schedule(task, showTime, TimeUnit.SECONDS);
        }
    }

    /**
     * Ideally this would be a logo of the webpage or something that gets passed with the transaction.
     * I currently have it set to the Tap logo. It could also be a preview for the webpage, but I suspect that wouldn't
     * be handled the same way as fetching an image. Need to ask Katherine about it.
     *
    @Override
    public void onImageRetrieved(Bitmap image) {
        imageView.setImageBitmap(image);
    }
    **/

    /**
     * This makes it go to the arm screen when the user hits back.
     */
    // May or may not be a problem with this approach in the long-term
    @Override
    public void onBackPressed() {
        if (forceReturnToArmScreen) {
            Intent startMain = new Intent(this, MainActivity.class);
            startMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivity(startMain);
            finish();
        }
        else {
            finish();
        }
    }

}
