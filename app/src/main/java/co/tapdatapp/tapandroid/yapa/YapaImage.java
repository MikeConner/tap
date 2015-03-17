package co.tapdatapp.tapandroid.yapa;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import co.tapdatapp.tapandroid.MainActivity;
import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.localdata.Transaction;

public class YapaImage extends Activity implements TapBitmap.Callback {

    private boolean isImageFitToScreen = false;
    private ImageView imageView;
    private boolean forceReturnToArmScreen = false;

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_yapa_image);
        Bundle extras = getIntent().getExtras();
        final String transactionId = extras.getString(YapaDisplay.TRANSACTION_ID);
        final Transaction transaction = new Transaction();
        transaction.moveToSlug(transactionId);

        final Button fullButton = (Button) findViewById(R.id.full_screen_button);
        imageView = (ImageView)findViewById(R.id.yapaImage);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isImageFitToScreen) {
                    isImageFitToScreen=false;
                    imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
                    imageView.setAdjustViewBounds(true);
                    fullButton.setVisibility(View.VISIBLE);
                }
                else {
                    isImageFitToScreen=true;
                    imageView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    fullButton.setVisibility(View.GONE);
                }
            }
        });
        new TapBitmap().execute(this, transaction.getYapa_url());

        final TextView imageSender = (TextView) findViewById(R.id.image_sender);
        final TextView imageDescription = (TextView) findViewById(R.id.image_description);
        final TextView imageDate = (TextView) findViewById(R.id.image_date);
        imageSender.setText(transaction.getNickname());
        imageDescription.setText(transaction.getDescription());
        imageDate.setText(transaction.getTimestamp().toString() + "  " + Integer.toString(transaction.getAmount()));

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

    @Override
    public void onImageRetrieved(Bitmap image) {
        imageView.setImageBitmap(image);
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
            Intent startMain = new Intent(this, MainActivity.class);
            startActivity(startMain);
        }
        else {
            finish();
        }
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

}
