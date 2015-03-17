/**
 * Background thread to get the currency image if it's not already
 * available, and provide it to the ListView row.
 */

package co.tapdatapp.tapandroid.currency;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.TapBitmap;

public class SetBalanceImageTask extends AsyncTask<Object, Void, Void> {

    private Bitmap image = null;
    private ImageView view;

    @Override
    protected Void doInBackground(Object... params) {
        if (params.length != 3) {
            throw new AssertionError(
                "Must provide ImageView, url, and target size"
            );
        }
        view = (ImageView)params[0];
        String url = (String)params[1];
        int imageSize = (int)params[2];
        try {
            image = Bitmap.createScaledBitmap(
                TapBitmap.fetchFromCacheOrWeb(url),
                imageSize,
                imageSize,
                true
            );
        }
        catch (Throwable t) {
            TapApplication.handleFailures(t);
        }
        return null;
    }

    protected void onPostExecute(Void v) {
        if (image != null) {
            view.setImageBitmap(image);
        }
    }

}
