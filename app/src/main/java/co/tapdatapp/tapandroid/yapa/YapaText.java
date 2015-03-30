package co.tapdatapp.tapandroid.yapa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import co.tapdatapp.tapandroid.MainActivity;
import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class YapaText extends Activity {

    private boolean forceReturnToArmScreen = false;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_text);
        Bundle extras = getIntent().getExtras();
        final String transactionId = extras.getString(YapaDisplay.TRANSACTION_ID);
        final Transaction transaction = new Transaction();
        transaction.moveToSlug(transactionId);

        final TextView textSender = (TextView) findViewById(R.id.text_sender);
        final TextView textDescription = (TextView) findViewById(R.id.text_description);
        final TextView textDate = (TextView) findViewById(R.id.text_date);

        textSender.setText(transaction.getNickname());
        textDescription.setText(transaction.getDescription());
        textDate.setText(transaction.getTimestamp().toString() + "  " + Integer.toString(transaction.getAmount()));

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
     * Makes pressing back go to the arm screen.
     *
     * if the concern that this is bad turns out to be true, this also needs to be changed.
     */
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

