/**
 * Layer over the SQLite database that provides dynamic access to
 * transaction history as required by the history ListView
 */

package co.tapdatapp.tapandroid.history;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.helpers.UserFriendlyError;
import co.tapdatapp.tapandroid.localdata.BaseAdapter;
import co.tapdatapp.tapandroid.localdata.Transaction;
import co.tapdatapp.tapandroid.localdata.TransactionDAO;
import co.tapdatapp.tapandroid.localdata.Yapa;
import co.tapdatapp.tapandroid.yapa.YapaDisplay;

public class HistoryAdapter extends BaseAdapter {

    private Integer recordCount = null;
    private TransactionDAO dao;
    private Activity activity;
    private Transaction transaction;

    public HistoryAdapter(TransactionDAO t, Activity a) {
        dao = t;
        activity = a;
        transaction = new Transaction();
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public int getCount() {
        setCount();
        return recordCount;
    }

    @Override
    public Object getItem(int i) {
        return dao.getByOrder(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int i, View v, ViewGroup viewGroup) {
        if (v == null) {
            LayoutInflater inflater =
                (LayoutInflater) TapApplication.get().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE
                );
            v = inflater.inflate(
                R.layout.history_grid_item,
                viewGroup,
                false
            );
        }
        transaction.moveToByOrder(i);
        ((TextView)v.findViewById(R.id.history_text)).setText(transaction.getDescription());
        ImageView historyIcon = ((ImageView)v.findViewById(R.id.history_icon));
        ImageView historyPreview = ((ImageView)v.findViewById(R.id.history_preview));
        if (transaction.getContentType().equals(Yapa.TYPE_IMAGE)) {
            new ImageFetchTask().execute(historyPreview, transaction);
        }
        YapaDisplay yl = new YapaDisplay();
        historyIcon.setImageDrawable(yl.getIcon(transaction));
        return v;
    }

    @Override
    public int getItemViewType(int i) {
        return R.layout.history_grid_item;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        setCount();
        return recordCount == 0;
    }

    /**
     * Get the number of records and cache them in a class variable
     */
    private void setCount() {
        if (recordCount == null) {
            recordCount = dao.getRecordCount();
        }
    }

    /**
     * Load the image onto the view in the background. This has to be a background task because
     * the image may not be in the local cache, and thus a network fetch would be required.
     */
    private class ImageFetchTask extends AsyncTask<Object, Void, Void> {

        Bitmap imageBitmap = null;
        ImageView imageView = null;

        /**
         * @param params ImageView to set and Transaction to set from
         * @return Void
         */
        @Override
        protected Void doInBackground(Object... params) {
            if (params.length != 2) {
                throw new AssertionError("Requires ImageView and transaction");
            }
            imageView = (ImageView)params[0];
            Transaction transaction = (Transaction)params[1];
            try {
                imageBitmap = TapBitmap.fetchFromCacheOrWeb(transaction.getYapa_url());
            }
            catch (Exception e) {
                imageFetchFailure(e);
            }
            return null;
        }

        protected void onPostExecute(Void x) {
            //noinspection StatementWithEmptyBody
            if (imageBitmap != null) {
                Context context = activity.getApplicationContext();
                Resources res = context.getResources();
                BitmapDrawable yapaImage = new BitmapDrawable(res, imageBitmap);
                yapaImage.setAlpha(130);
                imageView.setImageDrawable(yapaImage);
            }
            else {
                // @TODO provide some sort of message to the user that the image can't be displayed
            }
        }

        /**
         * This will get called if the Image Fetch fails
         * @param t contains information on the error
         */
        protected void imageFetchFailure(Throwable t){
            try {
                throw t;
            }
            catch(UserFriendlyError ufe){
                TapApplication.errorToUser(ufe);
            }
            catch(Throwable catchall){
                TapApplication.unknownFailure(t);
            }
        }
    }
}
