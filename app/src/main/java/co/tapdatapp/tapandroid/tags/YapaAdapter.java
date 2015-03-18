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

    private Yapa[] yapa;

    /**
     * Constructor establishes adapter parameters
     *
     * @param _tag Initialize with Yapa for this tag
     */
    public YapaAdapter(Tag _tag) {
        yapa = _tag.getYapa();
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
        return yapa.length;
    }

    @Override
    public Object getItem(int position) {
        return yapa[position];
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
            v = inflater.inflate(
                getItemViewType(position),
                parent,
                false
            );
            new YapaLineItem(v);
        }
        Yapa y = yapa[position];
        YapaLineItem vh = (YapaLineItem)v.getTag();
        vh.setValues(y);
        return v;
    }

    @Override
    public int getItemViewType(int position) {
        switch (yapa[0].getType()) {
            case Yapa.TYPE_TEXT :
                return R.layout.line_item_yapa_text;
            case Yapa.TYPE_IMAGE :
                return R.layout.line_item_yapa_image;
            default :
                throw new AssertionError("Unknown yapa type " + yapa[0].getType());
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
