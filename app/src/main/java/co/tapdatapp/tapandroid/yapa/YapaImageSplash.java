package co.tapdatapp.tapandroid.yapa;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.tapdatapp.tapandroid.MainActivity;
import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class YapaImageSplash extends Activity {

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
        final Button fullButton = (Button) findViewById(R.id.full_screen_button);

        new ImageFetchTask().execute(imageView, transaction);

        imageSender.setText(transaction.getNickname());
        imageDescription.setText(transaction.getDescription());
        imageDate.setText(transaction.getTimestamp().toString() + "  " + Integer.toString(transaction.getAmount()));

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isImageFitToScreen) {
                    isImageFitToScreen=false;
                    imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                    imageView.setAdjustViewBounds(true);
                    fullButton.setVisibility(View.VISIBLE);
                }else{
                    isImageFitToScreen=true;
                    imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    fullButton.setVisibility(View.GONE);
                }
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
     * This is another way of making the image fullscreen. It opens a new activity that is just a full screen image.
     * It's commented-out because it is causing an error with the grid view adapter.
     */
    /**public void makeFull(View view){
        /**
         * Currently reverting back to an old method of displaying full screen images.
         */
        /**Intent fullScreenIntent = new Intent(YapaImage.this, FullScreenImage.class);
        //fullScreenIntent.putExtra(FullScreenImage.TRANSACTION_ID,transactionId);
        startActivity(fullScreenIntent);
        **/
        /**final ImageView imageView = (ImageView) findViewById(R.id.yapaImage);

        if(isImageFitToScreen) {
            isImageFitToScreen=false;
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            imageView.setAdjustViewBounds(true);
        }else{
            isImageFitToScreen=true;
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
    }**/

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
                imageBitmap = TapBitmap.fetchFromCacheOrWeb(transaction.getYapa_url());
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
