package co.tapdatapp.tapandroid.yapa;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class YapaText extends Activity {

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_text);

        final TextView textSender = (TextView) findViewById(R.id.text_sender);
        final TextView textDescription = (TextView) findViewById(R.id.text_description);
        final TextView textDate = (TextView) findViewById(R.id.text_date);

        /**
         * Commented out Timestamps, was causing a crash
         */
        Transaction transaction = new Transaction();
        ((TextView)textSender.findViewById(R.id.text_sender)).setText(transaction.getNickname());
        ((TextView)textDescription.findViewById(R.id.text_description)).setText(transaction.getDescription());
        //((TextView)textDate.findViewById(R.id.text_date)).setText(transaction.getTimestamp().toString()
          //      + "  " + Integer.toString(transaction.getAmount()));
    }

    public void onResume() {
        super.onResume();
    }

    public void onPause(){
        super.onPause();
    }
}

