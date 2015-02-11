package co.tapdatapp.tapandroid.remotedata;

import android.os.AsyncTask;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import co.tapdatapp.tapandroid.R;

public class NewTagTask extends AsyncTask<NewTagTask.Callback, Void, Void> {

    /**
     * The Callback will receive the results of the operation
     */
    public interface Callback {
        /**
         * Called if the tag is successfully created by the server
         *
         * @param tag The created tag
         */
        void newTagReturned(TagCodec tag);

        /**
         * Called if any errors occur
         *
         * @param t Exception containing error details
         */
        void createTagFailure(Throwable t);
    }

    private boolean success;
    private TagCodec tag;
    private Throwable error;
    private Callback callback;

    @Override
    protected Void doInBackground(Callback... params) {
        if (params.length != 1) {
            throw new AssertionError("Must provide one callback");
        }
        callback = params[0];
        try {
            tag = getTagFromServer();
            success = true;
        }
        catch (Throwable t) {
            success = false;
            error = t;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void v) {
        if (success) {
            callback.newTagReturned(tag);
        }
        else {
            callback.createTagFailure(error);
        }
    }

    /**
     * Actually do the server request. Keeping this in its own method
     * facilitates testing.
     *
     * @return Tag object
     * @throws JSONException if the response doesn't parse
     * @throws WebServiceError On network/server errors
     */
    public TagCodec getTagFromServer()
    throws JSONException, WebServiceError {
        HttpHelper http = new HttpHelper();
        JSONObject response = http.HttpPostJSON(
            http.getFullUrl(R.string.ENDPOINT_NEW_TAG),
            new Bundle(),
            new JSONObject()
        );
        return new TagCodec(response);
    }

}
