/**
 * Pull down the list of tags into local storage
 */

package co.tapdatapp.tapandroid.tags;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Tag;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.TagCodec;
import co.tapdatapp.tapandroid.remotedata.WebResponse;
import co.tapdatapp.tapandroid.remotedata.YapaCodec;

public class SyncTagsTask extends AsyncTask<SyncTagsTask.Callback, Void, Void> {

    public interface Callback {
        /**
         * Called when the tags have successfully been synchronized
         */
        void onTagsSynced();

        /**
         * Called if tag synchronization fails
         *
         * @param t The cause of the failure
         */
        void onTagSyncFailed(Throwable t);
    }

    private Throwable error = null;
    private Callback callback;

    @Override
    protected Void doInBackground(Callback... params) {
        if (params.length != 1) {
            throw new AssertionError("Must provide one callback class");
        }
        callback = params[0];
        try {
            syncAllTags();
        }
        catch (Throwable t) {
            error = t;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if (error == null) {
            callback.onTagsSynced();
        }
        else {
            callback.onTagSyncFailed(error);
        }
    }

    public void syncAllTags() throws Exception {
        HttpHelper httpHelper = new HttpHelper();
        WebResponse wr = httpHelper.HttpGet(
            httpHelper.getFullUrl(R.string.ENDPOINT_TAGS),
            new Bundle()
        );
        JSONObject output = wr.getJSON();
        JSONArray tags;
        try {
            tags = output.getJSONArray("response");
        }
        catch (JSONException je) {
            // @TODO fix this on the server, an empty array is not the same as a null object
            if (je.getMessage().startsWith("No value for")) {
                return;
            }
            else {
                throw je;
            }
        }
        int length = tags.length();
        TagCodec tagCodec = new TagCodec();
        YapaCodec yapaCodec = new YapaCodec();
        Tag tag = new Tag();
        tag.removeAll();
        for (int i = 0; i < length; i++) {
            tagCodec.parse(tags.getJSONObject(i));
            HashMap<String, String> idMap = new HashMap<>();
            idMap.put("tag_id", tagCodec.getId());
            JSONObject response = httpHelper.HttpGetJSON(
                httpHelper.getFullUrl(
                    R.string.ENDPOINT_YAPA,
                    "",
                    idMap),
                new Bundle()
            );
            Log.d("YAPA_JSON", response.toString());
            yapaCodec.parse(response);
            tag.create(tagCodec, yapaCodec);
        }
    }

}
