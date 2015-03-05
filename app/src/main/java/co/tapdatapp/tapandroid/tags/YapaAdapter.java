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

    /**
     * Constructor establishes adapter parameters
     *
     * @param ocl triggered whenever data is changed
     * @param tagId Initialize with Yapa for this tag
     */
    public YapaAdapter(OnChangeListener ocl, String tagId) {
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

    private class ViewHolder {
        EditText etYapaText;
        TextView tvYapaThreshold;
        SeekBar seekYapaThreshold;
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
            vh.seekYapaThreshold.setTag(vh.tvYapaThreshold);
            v.setTag(vh);
        }
        Yapa y = yapa[position];
        ViewHolder vh = (ViewHolder)v.getTag();
        vh.etYapaText.setText(y.getContent());
        vh.tvYapaThreshold.setText(Integer.toString(y.getThreshold()));
        /**
         * A good max seems to depend more on the phone than anything
         * else, since too high of a max makes it difficult to select
         * a precise value. Of course, too low of a max will disallow
         * values that we might want to allow.
         */
        // @TODO find out what the max should be
        vh.seekYapaThreshold.setMax(30);
        vh.seekYapaThreshold.setProgress(y.getThreshold());
        vh.seekYapaThreshold.setOnSeekBarChangeListener(this);
        return v;
    }

    @Override
    public int getItemViewType(int position) {
        return R.layout.line_item_yapa;
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
            ((TextView)seekBar.getTag()).setText(Integer.toString(progress));
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
}
