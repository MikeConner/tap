package co.tapdatapp.tapandroid.yapa;

import android.app.Activity;
import android.content.Intent;
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
import co.tapdatapp.tapandroid.localdata.Transaction;

public class YapaAudio extends Activity {

    private boolean forceReturnToArmScreen = false;
    private ScheduledFuture futureTask;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_audio);
        Bundle extras = getIntent().getExtras();
        final String transactionId = extras.getString(YapaDisplay.TRANSACTION_ID);
        final Transaction transaction = new Transaction();
        transaction.moveToSlug(transactionId);
        final String yapaFullAudio = transaction.getURI();

        //This makes clicking the top frame open the audio in a different app.
        ImageView imageView = (ImageView)findViewById(R.id.yapa_audio_top_background);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(yapaFullAudio));
                startActivity(intent);
            }
        });

        //Clicking on the exit button emulates the back button
        ImageView quitButton = (ImageView) findViewById(R.id.exit_button_audio);
        quitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        TextView descriptionText = (TextView) findViewById(R.id.yapa_description_audio);
        TextView senderText = (TextView) findViewById(R.id.yapa_sender_audio);
        TextView timestampText = (TextView) findViewById(R.id.yapa_timestamp_audio);
        TextView amountText = (TextView) findViewById(R.id.yapa_amount_audio);
        TextView contentText = (TextView) findViewById(R.id.yapa_content_audio);

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
     *
     * I think the RAM problem is solved.
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

