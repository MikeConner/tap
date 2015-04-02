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

public class YapaImage extends Activity implements TapBitmap.Callback{

    private boolean forceReturnToArmScreen = false;
    private ScheduledFuture futureTask;
    private ImageView imageView;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_image);
        Bundle extras = getIntent().getExtras();
        final String transactionId = extras.getString(YapaDisplay.TRANSACTION_ID);
        final Transaction transaction = new Transaction();
        transaction.moveToSlug(transactionId);
        final String yapaFullImage = transaction.getYapa_url();

        //This makes clicking the top frame open the image in an image viewer.
        imageView = (ImageView)findViewById(R.id.yapa_image_top_background);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(yapaFullImage));
                startActivity(intent);
            }
        });

        new TapBitmap().execute(this, transaction.getYapa_url());

        //Clicking on the exit button emulates the back button
        ImageView quitButton = (ImageView) findViewById(R.id.exit_button_image);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView nameText = (TextView) findViewById(R.id.yapa_name_image);
        TextView senderText = (TextView) findViewById(R.id.yapa_sender_image);
        TextView timestampText = (TextView) findViewById(R.id.yapa_timestamp_image);
        TextView amountText = (TextView) findViewById(R.id.yapa_amount_image);
        TextView descriptionText = (TextView) findViewById(R.id.yapa_description_image);

        //nameText.setText(transaction.getName());
        senderText.setText(transaction.getNickname());
        timestampText.setText(transaction.getTimestamp().toString());
        amountText.setText(Integer.toString(transaction.getAmount()));
        descriptionText.setText(transaction.getDescription());

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
     * I think the RAM-depleting issue has been fixed
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

