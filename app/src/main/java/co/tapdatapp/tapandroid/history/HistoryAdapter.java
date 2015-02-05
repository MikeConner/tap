/**
 * Layer over the SQLite database that provides dynamic access to
 * transaction history as required by the history ListView
 */

package co.tapdatapp.tapandroid.history;

import android.app.Activity;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.localdata.Transaction;
import co.tapdatapp.tapandroid.localdata.TransactionDAO;

public class HistoryAdapter implements ListAdapter {

    private static Bitmap rewardBitmap;
    private static Bitmap iconBitmap;

    private DataSetObserver dso;
    private Integer recordCount = null;
    private TransactionDAO dao;
    private Activity activity;

    public HistoryAdapter(TransactionDAO t, Activity a) {
        dao = t;
        activity = a;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        if (dso != null) {
            throw new AssertionError("Attempt to set DataSetObserver 2x");
        }
        dso = dataSetObserver;
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        if (dso != dataSetObserver) {
            throw new AssertionError("Attempt to unregister non-registered DSO");
        }
        dso = null;
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
                R.layout.history_line_item,
                viewGroup,
                false
            );
        }
        Transaction t = new Transaction();
        t.moveTo(i);
        ((TextView)v.findViewById(R.id.history_li_date)).setText(t.getTimestamp().toString());
        ((TextView)v.findViewById(R.id.history_li_amount)).setText(Integer.toString(t.getAmount()));
        ((TextView)v.findViewById(R.id.history_li_description)).setText(t.getDescription());
        ((TextView)v.findViewById(R.id.history_li_recip_nick)).setText(t.getNickname());
        ((ImageView)v.findViewById(R.id.history_li_thumbnail)).setImageBitmap(getRewardBitmap());
        ((ImageView)v.findViewById(R.id.history_li_recip_image)).setImageBitmap(getIconBitmap());
        LoadHistoryImagesTask asyncLoad = new LoadHistoryImagesTask();
        asyncLoad.execute(v);
        return v;
    }

    @Override
    public int getItemViewType(int i) {
        return R.layout.history_line_item;
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
     * Return a reference to a properly sized Bitmap for the reward
     * that says "loading" or something similar until the real
     * Bitmap is retrieved
     *
     * @return default Bitmap
     */
    private Bitmap getRewardBitmap() {
        int size = getScreenWidth() / 6;
        if (rewardBitmap == null) {
            rewardBitmap = scaleBitmap(R.drawable.loading_square, size);
        }
        else {
            if (rewardBitmap.getWidth() != size) {
                rewardBitmap = scaleBitmap(R.drawable.loading_square, size);
            }
        }
        return rewardBitmap;
    }

    /**
     * Return a reference to a properly sized Bitmap for the user icon
     * that says "loading" or something until the real bitmap is
     * retrieved
     *
     * @return default bitmap
     */
    private Bitmap getIconBitmap() {
        int size = getScreenWidth() / 7;
        if (iconBitmap == null) {
            iconBitmap = scaleBitmap(R.drawable.loading_square, size);
        }
        else {
            if (iconBitmap.getWidth() != size) {
                iconBitmap = scaleBitmap(R.drawable.loading_square, size);
            }
        }
        return iconBitmap;
    }

    /**
     * Convenience wrapper to scale a square bitmap
     *
     * @param bitmap drawable ID
     * @param size desired size
     * @return Bitmap object of the resource resized to the target size
     */
    private Bitmap scaleBitmap(int bitmap, int size) {
        return Bitmap.createScaledBitmap(
            BitmapFactory.decodeResource(activity.getResources(), bitmap),
            size,
            size,
            true
        );
    }

    @SuppressWarnings("deprecation")
    private int getScreenWidth() {
        return activity.getWindowManager().getDefaultDisplay().getWidth();
    }

}
