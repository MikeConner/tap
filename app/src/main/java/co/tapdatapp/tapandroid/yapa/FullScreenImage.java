package co.tapdatapp.tapandroid.yapa;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ImageView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class FullScreenImage extends Activity
{

    public final static String TRANSACTION_ID = "TxId";
    Activity activity;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_image);
        final int transactionId = getIntent().getExtras().getInt(TRANSACTION_ID);
        final Transaction transaction = new Transaction();
        transaction.moveToByOrder(transactionId);
        final ImageView fullView = (ImageView) findViewById(R.id.full_screen_image);

        Context context = activity.getApplicationContext();
        Resources res = context.getResources();
        fullView.setImageDrawable(res.getDrawable(R.drawable.full_screen_logo));
       // new ImageFetchTask().execute(fullView,transaction);

    }

    /**
     * Copied this method from the YapaImage class, executing the FullScreenImage class as a subclass was causing problems
     */
  private class ImageFetchTask extends AsyncTask<Object, Void, Void> {

        Bitmap imageBitmap = null;
        ImageView imageView = null;

        /**
         * @param params ImageView to set and Transaction to set from
         * @return Void
         */
       // @Override
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
