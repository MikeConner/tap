package co.tapdatapp.tapandroid.history;

import android.app.Activity;
import android.content.Context;
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

public class HistoryActivityAdapter extends BaseAdapter{

    private static Bitmap rewardBitmap;
    private static Bitmap iconBitmap;

    private Integer recordCount = null;
    private TransactionDAO dao;
    private Activity activity;

    public HistoryActivityAdapter(TransactionDAO t, Activity a){
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
        /**
         * Commented out a lot of text. It now only displays a picture a single piece of text.
         */
        Transaction t = new Transaction();
        t.moveTo(i);
        //((TextView)v.findViewById(R.id.history_li_date)).setText(t.getTimestamp().toString());
        //((TextView)v.findViewById(R.id.history_li_amount)).setText(Integer.toString(t.getAmount()));
        ((TextView)v.findViewById(R.id.history_text)).setText(t.getDescription());
        //((TextView)v.findViewById(R.id.history_li_recip_nick)).setText(t.getNickname());
        ((ImageView)v.findViewById(R.id.history_picture)).setImageBitmap(getRewardBitmap());
        //((ImageView)v.findViewById(R.id.history_li_recip_image)).setImageBitmap(getIconBitmap());
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



