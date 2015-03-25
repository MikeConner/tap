/**
 * Helper class to encapsulate common operations on Bitmaps
 *
 * Can also be used as an AsyncTask to fetch an image locally
 */

package co.tapdatapp.tapandroid.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.UUID;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.localdata.AndroidCache;
import co.tapdatapp.tapandroid.localdata.Cache;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.RemoteStorage;
import co.tapdatapp.tapandroid.remotedata.RemoteStorageDriver;
import co.tapdatapp.tapandroid.remotedata.WebResponse;

public class TapBitmap extends AsyncTask<Object, Void, Void> {

    /**
     * Implement this interface in UI code to easily use this class
     * to fetch images.
     */
    public interface Callback {
        /**
         * Method called when an image is successfully fetched to
         * local storage.
         *
         * @param image Bitmap of the requested image
         */
        void onImageRetrieved(Bitmap image);
    }

    private Callback callback;
    private Throwable error = null;
    private Bitmap image;

    /**
     * Fetch an image in the background and call back to the UI
     * when complete or if an error occurs.
     *
     * @param params Callback UI object and image ID
     * @return Void
     */
    @Override
    protected Void doInBackground(Object... params) {
        if (params.length != 2) {
            throw new AssertionError("Must provide callback and image URL");
        }
        callback = (Callback)params[0];
        String imageId = (String)params[1];
        try {
            image = fetchFromCacheOrWeb(imageId);
        }
        catch (Exception e) {
            error = e;
        }
        return null;
    }

    /**
     * Call back to the UI code with either an error or success
     *
     * @param v nothing
     */
    @Override
    protected void onPostExecute(Void v) {
        if (error == null) {
            callback.onImageRetrieved(image);
        }
        else {
            TapApplication.handleFailures(error);
        }
    }

    /**
     * Get a Bitmap based on the URL. First looks to see if the local
     * cache has it, then fetches it from the URL if necessary.
     *
     * @param url where to fetch from the web
     * @return Bitmap specified by the URL
     * @throws Exception on any network issue
     */
    // The assignments to null are to free space on the heap as
    // otherwise this method can use 3x the size of the bitmap
    @SuppressWarnings("UnusedAssignment")
    public static
    Bitmap fetchFromCacheOrWeb(String url) throws Exception {
        AndroidCache cache = new AndroidCache();
        byte[] data;
        try {
            return fetchFromCache(url, cache);
        }
        catch (NoSuchElementException nsee) {
            HttpHelper helper = new HttpHelper();
            WebResponse wr = helper.HttpGet(url, new Bundle());
            if (wr.isOK()) {
                data = wr.getBody();
                String mediaType = wr.getMediaType();
                wr = null;
                if (data == null) {
                    throw new NullPointerException("Null data from webservice");
                }
                if (data.length == 0) {
                    throw new NullPointerException("0 length data from webservice");
                }
                cache.put(url, mediaType, data);
                data = null;
                return fetchFromCache(url, cache);
            }
            else {
                throw new Exception("Failure to fetch image: " + wr.getError());
            }
        }
    }

    @SuppressWarnings("ThrowFromFinallyBlock")
    public static Bitmap fetchFromCache(String url, AndroidCache cache) {
        InputStream is = null;
        try {
            is = cache.getStream(url);
            return BitmapFactory.decodeStream(is);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException ioe) {
                    // If we get an IOException closing a read-only
                    // stream, the phone is broken
                    throw new AssertionError(ioe);
                }
            }
        }
    }

    /**
     * Create a thumbnail version of the provided Bitmap and push to
     * remote (Amazon) storage.
     *
     * @param is InputStream pointing to the source image
     * @param size size of the thumbnail
     * @return ID of a new image in the cache
     */
    public static String
    storeThumbnailRemote(InputStream is, int size) throws Exception {
        byte[] byteArray = getResizedBytes(is, size);
        RemoteStorageDriver driver = RemoteStorage.getDriver();
        return driver.store(byteArray);
    }

    /**
     * Create a thumbnail version of the provided Bitmap and store it
     * locally only.
     *
     * @param is InputStream pointing to the source image
     * @param size size of the thumbnail
     * @return ID of a new image in the cache
     */
    public static String
    storeThumbnailLocal(InputStream is, int size) throws Exception {
        byte[] byteArray = getResizedBytes(is, size);
        Cache cache = new AndroidCache();
        String name = UUID.randomUUID().toString();
        cache.put(name, "image/jpeg", byteArray);
        return name;
    }

    /**
     * Do the heavy lifting of creating a resized image
     *
     * @param is InputStream pointing to the source image
     * @param size size of the thumbnail
     * @return byte array of the resized thumbnail Bitmap
     */
    private static byte[] getResizedBytes(InputStream is, int size) {
        Bitmap bmp = BitmapFactory.decodeStream(is);
        if (bmp == null) {
            throw new AssertionError("data failed to decode into a Bitmap");
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bmp = ThumbnailUtils.extractThumbnail(bmp, size, size);
        if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
            throw new AssertionError("Failed to compress image");
        }
        return outputStream.toByteArray();
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
