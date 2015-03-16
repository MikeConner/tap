/**
 * Background job to save the profile picture and update the account
 * information with it.
 */
package co.tapdatapp.tapandroid.user;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import co.tapdatapp.tapandroid.helpers.Files;
import co.tapdatapp.tapandroid.remotedata.RemoteStorage;
import co.tapdatapp.tapandroid.remotedata.RemoteStorageDriver;

public class SaveProfilePicTask extends AsyncTask<Object, Void, Void> {

    public interface Callback {
        void onProfilePicSaved(String id);
        void onProfilePicSaveFailed(Throwable t);
    }

    private Callback callback;
    private Throwable error;
    private String id;
    private Context context;

    /**
     * Store the picture on AWS or the dev server, update the
     * user's info
     *
     * @param params Callback, image data
     * @return nothing
     */
    @Override
    protected Void doInBackground(Object... params) {
        if (params.length != 2) {
            throw new AssertionError("Must provide callback and InputStream");
        }
        callback = (Callback)params[0];

        Bitmap bmp;
        InputStream is = (InputStream)params[1];


        try {
            bmp = BitmapFactory.decodeStream(is);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bmp = ThumbnailUtils.extractThumbnail(bmp, 512, 512);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            byte[] byteArray = outputStream.toByteArray();
//            byte[] data = Files.readAllBytes(is);
            RemoteStorageDriver driver = RemoteStorage.getDriver();
            id = driver.store(byteArray);
            new Account().setProfilePicThumbUrl(id);
            new UpdateUserInfoTask().updateUser();
        }
        catch (Throwable e) {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException ioe) {
                    throw new AssertionError(ioe);
                }
            }
            error = e;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if (error == null) {
            callback.onProfilePicSaved(id);
        }
        else {
            callback.onProfilePicSaveFailed(error);
        }
    }

}
