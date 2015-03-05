package co.tapdatapp.tapandroid.yapa;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import co.tapdatapp.tapandroid.MainActivity;
import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class YapaUrlSplash extends Activity{

    public final static String TRANSACTION_ID = "TxId";
    private static final ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_url);

        final int transactionId = getIntent().getExtras().getInt(TRANSACTION_ID);
        final Transaction transaction = new Transaction();
        transaction.moveTo(transactionId);
        final ImageView imageView = (ImageView) findViewById(R.id.yapaUrl);
        final TextView urlSender = (TextView) findViewById(R.id.url_sender);
        final TextView urlDescription = (TextView) findViewById(R.id.url_description);
        final TextView urlDate = (TextView) findViewById(R.id.url_date);

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

        urlSender.setText(transaction.getNickname());
        urlDescription.setText(transaction.getDescription());
        urlDate.setText(transaction.getTimestamp().toString() + "  " + Integer.toString(transaction.getAmount()));

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

    /**
     * This makes clicking on the back button go back to the arm screen instead of the armed screen.
     */
    public void onBackPressed() {
        Intent startMain = new Intent(this, MainActivity.class);
        startActivity(startMain);
    }

    /**
     * Load the image onto the view in the background. This has to be a background task because
     * the image may not be in the local cache, and thus a network fetch would be required.
     */
    private class ImageFetchTask extends AsyncTask<Object, Void, Void> {

        Bitmap imageBitmap = null;
        ImageView imageView = null;

        /**
         * @param params ImageView to set and Transaction to set from
         * @return Void
         */
        @Override
        protected Void doInBackground(Object... params) {
            if (params.length != 2) {
                throw new AssertionError("Requires ImageView and transaction");
            }
            imageView = (ImageView)params[0];
            Transaction transaction = (Transaction)params[1];
            try {
                imageBitmap = TapBitmap.fetchFromCacheOrWeb(transaction.getThumb_url());
            }
            catch (Exception e) {
                TapApplication.unknownFailure(e);
            }
            return null;
        }

        protected void onPostExecute(Void x) {
            //noinspection StatementWithEmptyBody
            if (imageBitmap != null) {
                imageView.setImageBitmap(imageBitmap);
            }
            else {
                // @TODO provide some sort of message to the user that the image can't be displayed
            }
        }
    }
}
