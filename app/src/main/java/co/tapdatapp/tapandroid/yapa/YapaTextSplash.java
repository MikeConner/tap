package co.tapdatapp.tapandroid.yapa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import co.tapdatapp.tapandroid.MainActivity;
import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class YapaTextSplash extends Activity {
    private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_text);
        final int transactionId = getIntent().getExtras().getInt(YapaDisplay.TRANSACTION_ID);
        final Transaction transaction = new Transaction();
        transaction.moveToByOrder(transactionId);

        final TextView textSender = (TextView) findViewById(R.id.text_sender);
        final TextView textDescription = (TextView) findViewById(R.id.text_description);
        final TextView textDate = (TextView) findViewById(R.id.text_date);

        /**
         * This makes the Yapa appear for 5 seconds, then disappear.
         */
        Runnable task = new Runnable() {
            @Override
            public void run() {
                onBackPressed();
            }
        };
        worker.schedule(task, 5, TimeUnit.SECONDS);

        textSender.setText(transaction.getNickname());
        textDescription.setText(transaction.getDescription());
        textDate.setText(transaction.getTimestamp().toString() + "  " + Integer.toString(transaction.getAmount()));
    }

    /**
     * This makes clicking on the back button go back to the arm screen instead of the armed screen.
     */
    public void onBackPressed() {
        Intent startMain = new Intent(this, MainActivity.class);
        startActivity(startMain);
    }
}

