/**
 * Effectively a "viewholder" class, but with embellishments.
 */

package co.tapdatapp.tapandroid.tags;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Yapa;

abstract public class YapaLineItem
implements SeekBar.OnSeekBarChangeListener, TextWatcher {

    public static final int SELECT_PICTURE = 1;

    // Fields that apply to all Yapa types
    private final EditText etYapaDescription;
    private final TextView tvYapaThreshold;
    private final SeekBar seekYapaThreshold;

    protected Yapa yapa;
    protected ManageTagActivity activity;

    /**
     * Instantiate and return the appropriate class to match the type
     * of layout/type of yapa being displayed
     *
     * @param v The view that will display the Yapa
     * @param viewLayout The ID of the layout
     * @return A descendant of YapaLineItem appropriate to the yapa type
     */
    public static YapaLineItem
    setTypeSpecificViewHolder(ManageTagActivity a, View v, int viewLayout) {
        switch (viewLayout) {
            case R.layout.line_item_yapa_text :
                return new TextYapaLineItem(a, v);
            case R.layout.line_item_yapa_image :
                return new ImageYapaLineItem(a, v);
            default :
                throw new AssertionError("Unknown Yapa view type " + v.getId());
        }
    }

    /**
     * Constructor sets pointers to all objects in the view. Should
     * be overridden by individual yapa types so they can set
     * pointers to their type-specific fields.
     */
    protected YapaLineItem(ManageTagActivity a, View v) {
        activity = a;
        etYapaDescription = (EditText)v.findViewById(R.id.etYapaText);
        etYapaDescription.addTextChangedListener(this);
        tvYapaThreshold = (TextView)v.findViewById(R.id.tvYapaThreshold);
        seekYapaThreshold = (SeekBar)v.findViewById(R.id.seekYapaThreshold);
        seekYapaThreshold.setTag(v);
        // A good max seems to depend more on the phone than anything
        // else, since too high of a max makes it difficult to select
        // a precise value. Of course, too low of a max will disallow
        // values that we might want to allow.
        // @TODO Make this n -> n+10 where n is the lowest allowed value
        seekYapaThreshold.setMax(30);
        seekYapaThreshold.setOnSeekBarChangeListener(this);
        v.setTag(this);
    }

    /**
     * Set the display values from the passed object. This should
     * be overridden by the classes that implement the individual
     * Yapa types so they can set additional values as needed.
     */
    protected void setValues(Yapa y) {
        yapa = y;
        setThreshold(y.getThreshold());
        etYapaDescription.setText(y.getContent());
    }

    /**
     * Set the threshold on both the slider and the text
     */
    private void setThreshold(int to) {
        seekYapaThreshold.setProgress(to);
        tvYapaThreshold.setText(Integer.toString(to));
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
            tvYapaThreshold.setText(Integer.toString(progress));
            updateYapaRecord(false);
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
     * Update the Yapa object with the data from the view
     *
     * @param changed whether callers have changed the data in the Yapa
     */
    protected void updateYapaRecord(boolean changed) {
        changed |= yapa.setThresholdIfChanged(
            seekYapaThreshold.getProgress()
        );
        changed |= yapa.setDescriptionIfChanged(
            etYapaDescription.getText().toString()
        );
        activity.onChange(changed);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        // do nothing here
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Do nothing here
    }

    /**
     * When any text is changed, save the view back to SQL
     */
    @Override
    public void afterTextChanged(Editable s) {
        updateYapaRecord(false);
    }
}
