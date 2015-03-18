/**
 * Save the specified tag to the server in the background
 */

package co.tapdatapp.tapandroid.tags;

import android.os.AsyncTask;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Tag;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.TagCodec;
import co.tapdatapp.tapandroid.remotedata.WebServiceError;

public class SaveTagToServerTask
extends AsyncTask<Object, Void, String> {

    public interface Callback {
        void onTagSaved(String TagId);
        void onTagSaveFailed(Throwable t);
    }

    private Callback callback;
    private Throwable error;

    @Override
    protected String doInBackground(Object... params) {
        if (params.length != 2) {
            throw new AssertionError("Must provide callback and tag ID");
        }
        callback = (Callback)params[0];
        String tagId = (String)params[1];
        Tag tag = new Tag();
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
    public String saveTag(Tag t)
    throws IOException, JSONException, WebServiceError {
        TagCodec codec = new TagCodec();
        HttpHelper helper = new HttpHelper();
        if (Tag.NEW_TAG_ID.equals(t.getTagId())) {
            // Creating new tag
            JSONObject response = helper.HttpPostJSON(
                helper.getFullUrl(R.string.ENDPOINT_TAGS),
                new Bundle(),
                codec.marshallFullTag(t)
            );
            codec.parseSavedTagResponse(response);
            // @TODO update the local tag data with the new tag ID
            t.setTagId(codec.getId());
        }
        else {
            // Updating existing tag
            helper.HttpPutJSON(
                helper.getFullUrl(R.string.ENDPOINT_TAGS),
                new Bundle(),
                codec.marshallFullTag(t)
            );
        }
        return t.getTagId();
    }
}
