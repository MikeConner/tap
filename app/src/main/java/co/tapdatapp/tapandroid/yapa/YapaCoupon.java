package co.tapdatapp.tapandroid.yapa;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import co.tapdatapp.tapandroid.MainActivity;
import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class YapaCoupon extends Activity implements TapBitmap.Callback {

    private boolean forceReturnToArmScreen = false;
    private ScheduledFuture futureTask;
    private ImageView imageView;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_coupon);
        Bundle extras = getIntent().getExtras();
        final String transactionId = extras.getString(YapaDisplay.TRANSACTION_ID);
        final Transaction transaction = new Transaction();
        transaction.moveToSlug(transactionId);
        final String yapaFullCoupon = transaction.getYapa_url();

        //This makes clicking the top frame open the coupon in another app.
        imageView = (ImageView)findViewById(R.id.yapa_coupon_top_background);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(yapaFullCoupon));
                startActivity(intent);
            }
        });

        new TapBitmap().execute(this, transaction.getYapa_url());

        //Clicking on the exit button emulates the back button
        Button quitButton = (Button) findViewById(R.id.exit_button_coupon);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView descriptionText = (TextView) findViewById(R.id.yapa_description_coupon);
        TextView senderText = (TextView) findViewById(R.id.yapa_sender_coupon);
        TextView timestampText = (TextView) findViewById(R.id.yapa_timestamp_coupon);
        TextView amountText = (TextView) findViewById(R.id.yapa_amount_coupon);
        TextView contentText = (TextView) findViewById(R.id.yapa_content_coupon);

        descriptionText.setText(transaction.getDescription());
        senderText.setText(transaction.getNickname());
        timestampText.setText(transaction.getTimestamp().toString());
        amountText.setText(Integer.toString(transaction.getAmount()));
        contentText.setText(transaction.getContent());

        //This is all related to the timer task from the arm screen.
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
            futureTask = YapaDisplay.delayWorker.schedule(task, showTime, TimeUnit.SECONDS);
        }
    }

    public void onImageRetrieved(Bitmap image) {
        imageView.setImageBitmap(image);
    }

    /**
     * This makes clicking on the back button go back to the arm screen
     * instead of the armed screen.
     *
     * I'm pretty sure the RAM issue has been solved.
     */
    @Override
    public void onBackPressed() {
        if (forceReturnToArmScreen) {
            futureTask.cancel(true);
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

