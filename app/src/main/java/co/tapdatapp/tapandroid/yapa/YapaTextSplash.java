package co.tapdatapp.tapandroid.yapa;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class YapaTextSplash extends Activity {

    public final static String TRANSACTION_ID = "TxId";

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_text);
        final int transactionId = getIntent().getExtras().getInt(TRANSACTION_ID);
        final Transaction transaction = new Transaction();
        transaction.moveTo(transactionId);

        final TextView textSender = (TextView) findViewById(R.id.text_sender);
        final TextView textDescription = (TextView) findViewById(R.id.text_description);
        final TextView textDate = (TextView) findViewById(R.id.text_date);

        textSender.setText(transaction.getNickname());
        textDescription.setText(transaction.getDescription());
        textDate.setText(transaction.getTimestamp().toString() + "  " + Integer.toString(transaction.getAmount()));
    }
}

