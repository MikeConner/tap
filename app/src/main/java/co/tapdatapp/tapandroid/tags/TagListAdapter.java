package co.tapdatapp.tapandroid.tags;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.localdata.BaseAdapter;
import co.tapdatapp.tapandroid.localdata.Tag;

public class TagListAdapter extends BaseAdapter {

    private Tag dao;

    public TagListAdapter() {
        dao = new Tag();
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
        return dao.getCount();
    }

    @Override
    public Object getItem(int position) {
        return dao.getByOrder(position);
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
    public View getView(int position, View v, ViewGroup viewGroup) {
        if (v == null) {
            LayoutInflater inflater =
                (LayoutInflater)TapApplication.get().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE
                );
            v = inflater.inflate(
                R.layout.line_item_tag,
                viewGroup,
                false
            );
        }
        dao.moveToByOrder(position);
        ((TextView)v.findViewById(R.id.TagLineItemName)).setText(dao.getName());
        ((TextView)v.findViewById(R.id.TagLineItemId)).setText(dao.getTagId());
        return v;
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.line_item_tag;
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
