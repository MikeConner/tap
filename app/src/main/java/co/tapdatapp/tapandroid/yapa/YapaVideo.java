package co.tapdatapp.tapandroid.yapa;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import co.tapdatapp.tapandroid.MainActivity;
import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class YapaVideo extends Activity {

    private boolean forceReturnToArmScreen = false;
    private ScheduledFuture futureTask;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_image);
        Bundle extras = getIntent().getExtras();
        final String transactionId = extras.getString(YapaDisplay.TRANSACTION_ID);
        final Transaction transaction = new Transaction();
        transaction.moveToSlug(transactionId);
        final String yapaFullImage = transaction.getYapa_url();

        //This makes clicking the top frame open the image in an image viewer.
        RelativeLayout imageView = (RelativeLayout)findViewById(R.id.yapa_top_image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(yapaFullImage));
                startActivity(intent);
            }
        });

        //Clicking on the exit button emulates the back button
        Button quitButton = (Button) findViewById(R.id.exit_button_image);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView descriptionText = (TextView) findViewById(R.id.yapa_description_image);
        TextView senderText = (TextView) findViewById(R.id.yapa_sender_image);
        TextView timestampText = (TextView) findViewById(R.id.yapa_timestamp_image);
        TextView amountText = (TextView) findViewById(R.id.yapa_amount_image);
        TextView contentText = (TextView) findViewById(R.id.yapa_content_image);

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

    /**
     * This makes clicking on the back button go back to the arm screen
     * instead of the armed screen.
     */
    // @TODO I don't think this is correct, I have a feeling it is
    // creating an infinite backstack that will eventually run the
    // device out of RAM if the user does a lot of transactions.
    // Research needs to be done.
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

