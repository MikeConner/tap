/**
 * Enhances YapaLineItem to handle the type-specific fields for a
 * text Yapa.
 */

package co.tapdatapp.tapandroid.tags;

import android.view.View;
import android.widget.EditText;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Yapa;

public class TextYapaLineItem extends YapaLineItem {

    private EditText etYapaContent;

    protected TextYapaLineItem(ManageTagActivity a, View v) {
        super(a, v);
        etYapaContent = (EditText)v.findViewById(R.id.etYapaContent);
        etYapaContent.addTextChangedListener(this);
    }

    @Override
    public void setValues(Yapa y) {
        etYapaContent.setText(y.getContent());
        super.setValues(y);
    }

    @Override
    protected void updateYapaRecord(boolean changed) {
        changed |= yapa.setContentIfChanged(
            etYapaContent.getText().toString()
        );
        super.updateYapaRecord(changed);
    }

}
