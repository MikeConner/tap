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

    public void setValues(Yapa y) {
        super.setValues(y);
        etYapaContent.setText(y.getContent());
    }

    protected void updateYapaRecord() {
        yapa.setContent(etYapaContent.getText().toString());
        super.updateYapaRecord();
    }

}
