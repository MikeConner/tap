/**
 * Pull down the list of tags into local storage
 */

package co.tapdatapp.tapandroid.tags;

import android.os.AsyncTask;
import android.os.Bundle;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.TagCodec;
import co.tapdatapp.tapandroid.remotedata.WebResponse;

public class SyncTagsTask extends AsyncTask<SyncTagsTask.Callback, Void, Void> {

    public interface Callback {
        /**
         * Called when the tags have successfully been synchronized
         */
        void onTagsSynced(TagList tagList);

        /**
         * Called if tag synchronization fails
         *
         * @param t The cause of the failure
         */
        void onTagSyncFailed(Throwable t);
    }

    private Throwable error = null;
    private Callback callback;
    private TagList tagList;

    @Override
    protected Void doInBackground(Callback... params) {
        if (params.length != 1) {
            throw new AssertionError("Must provide one callback class");
        }
        callback = params[0];
        try {
            tagList = syncAllTags();
        }
        catch (Throwable t) {
            error = t;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if (error == null) {
            callback.onTagsSynced(tagList);
        }
        else {
            callback.onTagSyncFailed(error);
        }
    }

    public TagList syncAllTags() throws Exception {
        TagList t = new TagList();
        HttpHelper httpHelper = new HttpHelper();
        WebResponse wr = httpHelper.HttpGet(httpHelper.getFullUrl(R.string.ENDPOINT_TAGS), new Bundle());
        JSONObject output = wr.getJSON();
        JSONArray tags;
        try {
            tags = output.getJSONArray("response");
        }
        catch (JSONException je) {
            // @TODO fix this on the server, an empty array is not the same as a null object
            if (je.getMessage().startsWith("No value for")) {
                return t;
            }
            else {
                throw je;
            }
        }
        int length = tags.length();
        TagCodec codec;
        for (int i = 0; i < length; i++) {
            codec = new TagCodec(tags.getJSONObject(i));
            t.put(codec.getId(), codec.getTag());
        }
        return t;
    }

}
/*
mAuthToken = auth_token;
    //TODO: This needs to move in to class instantiation, and we need to clean it up upon destroy
    mTapCloud = new TapCloud();
    JSONObject output = null;
    try {
    WebResponse wr = httpHelper.HttpGet(httpHelper.getFullUrl(R.string.ENDPOINT_TAGS), new Bundle());
    output = wr.getJSON();
    JSONArray jsonTags = new JSONArray();
    mtagMap = new HashMap<>();
    try {
    jsonTags = output.getJSONArray("response");
    }
    catch (JSONException je) {
    if (je.getMessage().startsWith("No value for")) {
    // In case of empty return value, return empty Map
    return mtagMap;
    }
    else {
    TapApplication.unknownFailure(je);
    }
    }
    int length = jsonTags.length();

    for (int i = 0; i < length; i++) {
    mtagMap.put(jsonTags.getJSONObject(i).getString("id"), jsonTags.getJSONObject(i).getString("name"));
    }
    return mtagMap;
    }
    catch (JSONException je) {
    Log.e("WEBSERVICE", output.toString());
    throw new WebServiceError(je);
    }
    catch (Exception e) {
    TapApplication.unknownFailure(e);
    throw new WebServiceError(e);
    }
    */