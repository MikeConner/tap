/**
 * Adapter to populate the ListView on ManageTagActivity
 */

package co.tapdatapp.tapandroid.tags;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.UUID;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.localdata.BaseAdapter;
import co.tapdatapp.tapandroid.localdata.Tag;
import co.tapdatapp.tapandroid.localdata.Yapa;

public class YapaAdapter
extends BaseAdapter
implements SeekBar.OnSeekBarChangeListener {

    public interface OnChangeListener {
        void onChange();
    }

    private OnChangeListener changeListener;
    private Yapa[] yapa;
    private String tagId;

    /**
     * Constructor establishes adapter parameters
     *
     * @param ocl triggered whenever data is changed
     * @param id Initialize with Yapa for this tag
     */
    public YapaAdapter(OnChangeListener ocl, String id) {
        tagId = id;
        changeListener = ocl;
        Tag tag = new Tag();
        tag.moveTo(tagId);
        yapa = tag.getYapa();
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

    /**
     * ViewHolder class to simplify object access
     */
    private class ViewHolder {
        EditText etYapaText;
        TextView tvYapaThreshold;
        SeekBar seekYapaThreshold;
        UUID slug;
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
            ViewHolder vh = new ViewHolder();
            vh.etYapaText = (EditText)v.findViewById(R.id.etYapaText);
            vh.tvYapaThreshold = (TextView)v.findViewById(R.id.tvYapaThreshold);
            vh.seekYapaThreshold = (SeekBar)v.findViewById(R.id.seekYapaThreshold);
            vh.seekYapaThreshold.setTag(v);
            v.setTag(vh);
        }
        Yapa y = yapa[position];
        ViewHolder vh = (ViewHolder)v.getTag();
        vh.etYapaText.setText(y.getContent());
        vh.tvYapaThreshold.setText(Integer.toString(y.getThreshold()));
        // A good max seems to depend more on the phone than anything
        // else, since too high of a max makes it difficult to select
        // a precise value. Of course, too low of a max will disallow
        // values that we might want to allow.
        // @TODO Make this n -> n+10 where n is the lowest allowed value
        vh.seekYapaThreshold.setMax(30);
        vh.seekYapaThreshold.setProgress(y.getThreshold());
        vh.seekYapaThreshold.setOnSeekBarChangeListener(this);
        vh.slug = y.getSlug();
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

    /**
     * Called when the threshold slider value is changed
     *
     * @param seekBar the seekbar that was changed
     * @param progress The current setting
     * @param fromUser indicates whether it was user triggered or programmatic
     */
    @Override
    public void
    onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            View v = (View)seekBar.getTag();
            ViewHolder vh = (ViewHolder)v.getTag();
            vh.tvYapaThreshold.setText(Integer.toString(progress));
            updateYapaRecord(v);
            changeListener.onChange();
        }
    }

    /**
     * Called when user starts dragging the slider around
     *
     * @param seekBar per spec
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // Do nothing here
    }

    /**
     * Called when the user stops dragging the slider
     *
     * @param seekBar per spec
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // Do nothing here
    }

    /**
     * Update the SQL Yapa record with the data from the view
     *
     * @param v The view displaying the yapa to be updated
     */
    private void updateYapaRecord(View v) {
        Yapa y = new Yapa();
        ViewHolder h = (ViewHolder)v.getTag();
        y.setSlug(h.slug);
        y.setTagId(tagId);
        // @TODO this is incomplete ... finish it
    }
}
