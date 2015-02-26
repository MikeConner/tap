/**
 * Layer over the SQLite database that provides dynamic access to
 * transaction history as required by the history ListView
 */

package co.tapdatapp.tapandroid.history;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.localdata.BaseAdapter;
import co.tapdatapp.tapandroid.localdata.Transaction;
import co.tapdatapp.tapandroid.localdata.TransactionDAO;

public class HistoryAdapter extends BaseAdapter {

    private static Bitmap rewardBitmap;
    private static Bitmap iconBitmap;

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
        transaction.moveTo(i);
        ((TextView)v.findViewById(R.id.history_text)).setText(transaction.getDescription());
        String yapaType = transaction.getContentType();
        ImageView historyPicture = ((ImageView)v.findViewById(R.id.history_picture));
        Context context = activity.getApplicationContext();
        Resources res = context.getResources();
        switch(yapaType){

            case "image":
                historyPicture.setImageDrawable(res.getDrawable(R.drawable.yapa_image));
                break;
            case "url":
                historyPicture.setImageDrawable(res.getDrawable(R.drawable.yapa_link));
                break;
            case "text":
                historyPicture.setImageDrawable(res.getDrawable(R.drawable._yapa_text));
                break;
            default:
                historyPicture.setImageBitmap(getRewardBitmap());

        }
        LoadHistoryImagesTask asyncLoad = new LoadHistoryImagesTask();
        asyncLoad.execute(v);
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
     * Return a reference to a properly sized Bitmap for the reward
     * that says "loading" or something similar until the real
     * Bitmap is retrieved
     *
     * @return default Bitmap
     */
    private Bitmap getRewardBitmap() {
        final int size = getScreenWidth(activity) / 6;
        if (rewardBitmap == null) {
            rewardBitmap = TapBitmap.getLoadingBitmapAtSize(size);
        }
        else {
            if (rewardBitmap.getWidth() != size) {
                rewardBitmap = TapBitmap.getLoadingBitmapAtSize(size);
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
        final int size = getScreenWidth(activity) / 7;
        if (iconBitmap == null) {
            iconBitmap = TapBitmap.getLoadingBitmapAtSize(size);
        }
        else {
            if (iconBitmap.getWidth() != size) {
                iconBitmap = TapBitmap.getLoadingBitmapAtSize(size);
            }
        }
        return iconBitmap;
    }

}
