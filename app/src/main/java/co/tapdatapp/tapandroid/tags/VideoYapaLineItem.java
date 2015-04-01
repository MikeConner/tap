/**
 * Enhances YapaLineItem to handle the type-specific fields for a
 * Video Yapa.
 */
package co.tapdatapp.tapandroid.tags;

import android.view.View;
import android.widget.EditText;
import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Yapa;


public class VideoYapaLineItem extends YapaLineItem {

    private EditText etYapaContent;

    protected VideoYapaLineItem(ManageTagActivity a, View v) {
        super(a, v);
        etYapaContent = (EditText)v.findViewById(R.id.etYapaContent);
        etYapaContent.addTextChangedListener(this);
    }

    @Override
    public void setValues(Yapa y) {
        super.setValues(y);
        etYapaContent.setText(y.getUri());
    }

    @Override
    protected void updateYapaRecord(boolean changed) {
        changed |= yapa.setUriIfChanged(
                etYapaContent.getText().toString()
        );
        super.updateYapaRecord(changed);
    }

}