package co.tapdatapp.tapandroid.yapa;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class YapaImage extends Activity {

    public final static String TRANSACTION_ID = "TxId";

    private boolean isImageFitToScreen = false;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_image);
        final int transactionId = getIntent().getExtras().getInt(TRANSACTION_ID);
        final Transaction transaction = new Transaction();
        transaction.moveTo(transactionId);
        final ImageView imageView = (ImageView) findViewById(R.id.yapaImage);
        final TextView imageSender = (TextView) findViewById(R.id.image_sender);
        final TextView imageDescription = (TextView) findViewById(R.id.image_description);
        final TextView imageDate = (TextView) findViewById(R.id.image_date);

        new ImageFetchTask().execute(imageView, transaction);

        imageSender.setText(transaction.getNickname());
        imageDescription.setText(transaction.getDescription());
        imageDate.setText(transaction.getTimestamp().toString() + "  " + Integer.toString(transaction.getAmount()));

        /**
         * This makes clicking on the image turn it into a fullscreen view.
         */
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fullScreenIntent = new Intent(v.getContext(), FullScreenImage.class);
                fullScreenIntent.putExtra(YapaImage.TRANSACTION_ID,transactionId);
                YapaImage.this.startActivity(fullScreenIntent);
            }
        });

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

    private class FullScreenImage extends Activity
    {
        public final static String TRANSACTION_ID = "TxId";
        protected void onCreate(Bundle savedInstanceState) {
            setContentView(R.layout.full_image);
            final int transactionId = getIntent().getExtras().getInt(TRANSACTION_ID);
            final Transaction transaction = new Transaction();
            transaction.moveTo(transactionId);
            ImageView fullView = (ImageView) findViewById(R.id.full_screen_image);
            fullView.setLayoutParams( new ViewGroup.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT));
            Bitmap imageBitmap = null;
            try {
                imageBitmap = TapBitmap.fetchFromCacheOrWeb(transaction.getThumb_url());
            }
            catch (Exception e) {
                TapApplication.unknownFailure(e);
            }

            fullView.setScaleType(ImageView.ScaleType.FIT_XY);

        }
    }

}
