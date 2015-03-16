/**
 * Background job to save the profile picture and update the account
 * information with it.
 */
package co.tapdatapp.tapandroid.user;

import android.os.AsyncTask;

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
        InputStream is = (InputStream)params[1];
        try {
            byte[] data = Files.readAllBytes(is);
            RemoteStorageDriver driver = RemoteStorage.getDriver();
            id = driver.store(data);
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