/**
 * Adapter to populate the ListView on ManageTagActivity
 */

package co.tapdatapp.tapandroid.tags;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.localdata.BaseAdapter;
import co.tapdatapp.tapandroid.localdata.Tag;
import co.tapdatapp.tapandroid.localdata.Yapa;

public class YapaAdapter extends BaseAdapter {

    private ManageTagActivity activity;
    private Tag tag;

    /**
     * Constructor establishes adapter parameters
     *
     * @param _tag Tag to retrived Yapa from
     */
    public YapaAdapter(ManageTagActivity a, Tag _tag) {
        activity = a;
        tag = _tag;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }

    @Override
    public int getCount() {
        return tag.getYapa().length;
    }

    @Override
    public Object getItem(int position) {
        return tag.getYapa()[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int position, View v, ViewGroup parent) {
        if (v == null) {
            LayoutInflater inflater =
                (LayoutInflater) TapApplication.get().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE
                );
            int viewType = getItemViewType(position);
            v = inflater.inflate(viewType, parent, false);
            YapaLineItem.setTypeSpecificViewHolder(activity, v, viewType);
        }
        ((YapaLineItem)v.getTag()).setValues(tag.getYapa()[position]);
        return v;
    }

    @Override
    public int getItemViewType(int position) {
        switch (tag.getYapa()[0].getType()) {
            case Yapa.TYPE_TEXT :
                return R.layout.line_item_yapa_text;
            case Yapa.TYPE_IMAGE :
                return R.layout.line_item_yapa_image;
            default :
                throw new AssertionError("Unknown yapa type " + tag.getYapa()[0].getType());
        }
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }

}
