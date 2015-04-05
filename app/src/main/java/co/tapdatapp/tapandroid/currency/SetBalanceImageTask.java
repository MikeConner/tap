/**
 * Background thread to get the currency image if it's not already
 * available, and provide it to the ListView row.
 */

package co.tapdatapp.tapandroid.currency;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.View;

import co.tapdatapp.tapandroid.helpers.TapBitmap;

public class SetBalanceImageTask extends AsyncTask<Object, Void, Void> {

    public interface Callback {
        void onBalanceImageFetched(View view, Bitmap image);
        void onBalanceImageFetchError(Throwable t);
    }

    private Bitmap image = null;
    private Callback callback;
    private Throwable error;
    private View view;

    @Override
    protected Void doInBackground(Object... params) {
        if (params.length != 3) {
            throw new AssertionError(
                "Must provide Callback, View, url, and target size"
            );
        }
        callback = (Callback)params[0];
        view = (View)params[1];
        String url = (String)params[2];
        int imageSize = (int)params[3];
        try {
            image = Bitmap.createScaledBitmap(
                TapBitmap.fetchFromCacheOrWeb(url),
                imageSize,
                imageSize,
                true
            );
        }
        catch (Throwable t) {
            error = t;
        }
        return null;
    }

    protected void onPostExecute(Void v) {
        if (image != null) {
            callback.onBalanceImageFetched(view, image);
            //view.setImageBitmap(image);
        }
        else {
            if (error != null) {
                callback.onBalanceImageFetchError(error);
            }
        }
    }

}
