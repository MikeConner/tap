/**
 * Helper class to encapsulate common operations on Bitmaps
 */

package co.tapdatapp.tapandroid.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.util.NoSuchElementException;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.localdata.AndroidCache;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.WebResponse;

public class TapBitmap {

    /**
     * Get a Bitmap based on the URL. First looks to see if the local
     * cache has it, then fetches it from the URL if necessary.
     *
     * @param url where to fetch from the web
     * @return Bitmap specified by the URL
     * @throws Exception on any network issue
     */
    public static
    Bitmap fetchFromCacheOrWeb(String url) throws Exception {
        Bitmap rv;
        AndroidCache cache = new AndroidCache();
        byte[] data;
        try {
            data = cache.get(url);
            rv = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (rv == null) {
                throw new NullPointerException(
                        "Null Bitmap from cache for " + url + " " + data.length
                );
            }
        }
        catch (NoSuchElementException nsee) {
            HttpHelper helper = new HttpHelper();
            WebResponse wr = helper.HttpGet(url, new Bundle());
            if (wr.isOK()) {
                data = wr.getBody();
                if (data == null) {
                    throw new NullPointerException("Null data from webservice");
                }
                if (data.length == 0) {
                    throw new NullPointerException("0 length data from webservice");
                }
                rv = BitmapFactory.decodeByteArray(data, 0, data.length);
                if (rv == null) {
                    throw new NullPointerException(
                        "Null bitmap from webservice for " + url
                    );
                }
                cache.put(url, wr.getMediaType(), data);
            }
            else {
                throw new Exception("Failure to fetch image: " + wr.getError());
            }
        }
        return rv;
    }

    /**
     * Convenience wrapper to scale a square bitmap
     *
     * @param bitmap drawable ID
     * @param size desired size
     * @return Bitmap object of the resource resized to the target size
     */
    public static Bitmap scaleBitmap(int bitmap, int size) {
        return Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(
                TapApplication.get().getResources(),
                bitmap
            ),
            size,
            size,
            true
        );
    }

    /**
     * Get the square "loading" image scaled to the specified size
     *
     * @param size in pixels
     * @return bitmap "Loading" at the specified size
     */
    public static Bitmap getLoadingBitmapAtSize(int size) {
        return scaleBitmap(R.drawable.loading_square, size);
    }
}
