/**
 * Save the specified tag to the server in the background
 */

package co.tapdatapp.tapandroid.tags;

import android.os.AsyncTask;
import android.os.Bundle;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.UUID;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.localdata.AndroidCache;
import co.tapdatapp.tapandroid.localdata.Tag;
import co.tapdatapp.tapandroid.localdata.Yapa;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.RemoteStorage;
import co.tapdatapp.tapandroid.remotedata.RemoteStorageDriver;
import co.tapdatapp.tapandroid.remotedata.TagCodec;
import co.tapdatapp.tapandroid.remotedata.WebResponse;
import co.tapdatapp.tapandroid.remotedata.WebServiceError;

public class SaveTagToServerTask
extends AsyncTask<Object, Void, String> {

    public interface Callback {
        void onTagSaved(String TagId);
        void onTagSaveFailed(Throwable t);
    }

    private Callback callback;
    private Throwable error;
    private AndroidCache cache;
    private HttpHelper helper;

    @Override
    protected String doInBackground(Object... params) {
        if (params.length != 2) {
            throw new AssertionError("Must provide callback and tag ID");
        }
        callback = (Callback)params[0];
        String tagId = (String)params[1];
        Tag tag = new Tag();
        helper = new HttpHelper();
        try {
            tag.moveTo(tagId);
            tagId = saveTag(tag);
        }
        catch (Throwable t) {
            error = t;
        }
        return tagId;
    }

    @Override
    protected void onPostExecute(String tagId) {
        if (error == null) {
            callback.onTagSaved(tagId);
        }
        else {
            callback.onTagSaveFailed(error);
        }
    }

    /**
     * Actually do the heavy lifting of saving the tag
     */
    public String saveTag(Tag t) throws Exception {
        saveAllYapaImages(t);
        TagCodec codec = new TagCodec();
        if (Tag.NEW_TAG_ID.equals(t.getTagId())) {
            // Creating new tag
            JSONObject response = helper.HttpPostJSON(
                helper.getFullUrl(R.string.ENDPOINT_TAGS),
                new Bundle(),
                codec.marshallFullTag(t)
            );
            codec.parseSavedTagResponse(response);
            t.setTagId(codec.getId());
        }
        else {
            // Updating existing tag
            Bundle headers = new Bundle();
            headers.putString("Accept", "application/json");
            headers.putString("Content-Type", "application/json");
            WebResponse response = helper.HttpPut(
                    getTagUpdateUrl(t.getTagId().replace("-", "")),
                    headers,
                    codec.marshallFullTag(t).toString()
            );
            if (!response.isOK()) {
                throw new WebServiceError(response);
            }
        }
        return t.getTagId();
    }

    private String getTagUpdateUrl(String tag) {
        return helper.getFullUrl(R.string.ENDPOINT_ONE_TAG, tag, new HashMap<String, String>());
    }

    /**
     * Save all the images in all the Yapa in the passed Tag to
     * network storage and update the URLs if any of them changed
     * as a result.
     */
    public void saveAllYapaImages(Tag t) throws Exception {
        Yapa[] list = t.getYapa();
        cache = new AndroidCache();
        for (Yapa y : list) {
            y.setImage(saveImage(y.getImage()));
            y.setThumb(saveImage(y.getThumb()));
        }
    }

    /**
     * Either create or update the data on remote storage, based on
     * whether the name is a URL or a UUID.
     *
     * @param name UUID for unsaved item, URL for item to be updated
     * @return The URL of the item as saved
     */
    public String saveImage(String name) throws Exception {
        if (name != null && !name.isEmpty()) {
            byte[] data;
            try {
                data = cache.get(name);
            }
            catch (NoSuchElementException nsee) {
                // If the element isn't available locally, then it
                // hasn't changed and there's nothing to do.
                return name;
            }
            RemoteStorageDriver storage = RemoteStorage.getDriver();
            try {
                //noinspection ResultOfMethodCallIgnored
                UUID.fromString(name);
                // If the name is successfully parsed as a UUID, then
                // it's temporarily stored in the local cache
                // @TODO I can find no reliable way to determine the
                // content-type of a file in Android, and hard-coding
                // JPEG here is a terrible assumption that is going to
                // wreck havoc if things ever change.
                return storage.store(data, TapBitmap.TYPE_JPEG);
            }
            catch (IllegalArgumentException iae) {
                // If we're unable to parse it as a UUID, then it
                // must be a URL where the data is already stored
                storage.overWrite(name, data);
                return name;
            }
        }
        else {
            return name;
        }
    }
}
