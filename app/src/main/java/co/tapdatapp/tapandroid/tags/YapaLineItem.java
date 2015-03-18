/**
 * Effectively a "viewholder" class, but with embellishments.
 *
 * Because yapa can be of many types and different types require
 * different behaviors, this class will function to abstract away
 * those differences and allow other code to make assumptions about
 * the Yapa that will simplify it, while allowing this class to do
 * any sanity checking necessary to keep things together.
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

public class YapaLineItem
implements SeekBar.OnSeekBarChangeListener, TextWatcher {
    private EditText etYapaDescription;
    private TextView tvYapaThreshold;
    private SeekBar seekYapaThreshold;
    private Yapa yapa;

    public YapaLineItem(View v) {
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

    public void setValues(Yapa y) {
        yapa = y;
        setDescription(y.getContent());
        setThreshold(y.getThreshold());
    }

    public void setDescription(String to) {
        etYapaDescription.setText(to);
    }

    public void setThreshold(int to) {
        seekYapaThreshold.setProgress(to);
        tvYapaThreshold.setText(Integer.toString(to));
    }

    public int getThreshold() {
        String value = tvYapaThreshold.getText().toString();
        if (value == null || value.isEmpty()) {
            return 0;
        }
        else {
            return Integer.parseInt(value);
        }
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
            updateYapaRecord();
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
     */
    private void updateYapaRecord() {
        yapa.setThreshold(getThreshold());
        yapa.setDescription(etYapaDescription.getText().toString());
        yapa.update();
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
        updateYapaRecord();
    }
}
