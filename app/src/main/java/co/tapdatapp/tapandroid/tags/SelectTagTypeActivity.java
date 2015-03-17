/**
 * Select the type of tag to create, then proceed to the tag management
 * screen.
 */

package co.tapdatapp.tapandroid.tags;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;

import java.util.UUID;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.Tag;
import co.tapdatapp.tapandroid.localdata.Yapa;

public class SelectTagTypeActivity extends Activity {

    private int selectedType = -1;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_select_tag_type);
    }

    @Override
    public void onResume() {
        super.onResume();
        fillInCurrencies();
        setButtonStatus();
    }

    /**
     * Triggered when a yapa type is selected
     *
     * @param v the button that was selected
     */
    public void onTypeSelect(View v) {
        if (v.isEnabled()) {
            findViewById(R.id.btnYapaImage).setSelected(false);
            findViewById(R.id.btnYapaText).setSelected(false);
            // @TODO uncomment these as the code is added
            //findViewById(R.id.btnYapaAudio).setSelected(false);
            //findViewById(R.id.btnYapaVideo).setSelected(false);
            //findViewById(R.id.btnYapaCoupon).setSelected(false);
            //findViewById(R.id.btnYapaUrl).setSelected(false);
            v.setSelected(true);
            selectedType = v.getId();
            setButtonStatus();
        }
    }

    /**
     * Enable/disable the create button based on whether all the
     * information is provided
     */
    private void setButtonStatus() {
        findViewById(R.id.btnCreateTag).setEnabled(
            selectedType != -1
        );
    }

    /**
     * Fill in the spinner with the list of available currencies and
     * enable it if the list is longer than 1
     */
    private void fillInCurrencies() {
        Spinner s = (Spinner)findViewById(R.id.spinnerSelectCurrency);
        MyCurrencyAdapter a = new MyCurrencyAdapter(this);
        s.setAdapter(a);
    }

    /**
     * When the "CREATE NEW TAG" button is clicked, create a local
     * copy of the tag and proceed to the management view.
     *
     * @param v The create new tag button
     */
    public void clickCreateTag(View v) {
        Tag tag = new Tag();
        tag.remove(ManageTagActivity.NEW_TAG);
        Yapa yapa = new Yapa();
        yapa.setTagId(ManageTagActivity.NEW_TAG);
        yapa.setThreshold(1);
        yapa.setSlug(UUID.randomUUID());
        yapa.setType(getTypeStringFromId());
        tag.create(ManageTagActivity.NEW_TAG, "New Tag", new Yapa[]{yapa});
        Intent i = new Intent(this, ManageTagActivity.class);
        i.putExtra(ManageTagActivity.MODE, ManageTagActivity.MODE_MODIFY);
        i.putExtra(ManageTagActivity.TAG_ID, ManageTagActivity.NEW_TAG);
        startActivity(i);
        finish();
    }

    /**
     * Convert the ID of the view that was picked to select a yapa
     * type into the string that is used to store it locally as well
     * as communicated with the server.
     *
     * @return String of the selected Yapa type
     */
    private String getTypeStringFromId() {
        switch (selectedType) {
            case R.id.btnYapaText :
                return Yapa.TYPE_TEXT;
            case R.id.btnYapaImage :
                return Yapa.TYPE_IMAGE;
            /*
            // @TODO uncomment these as the code is added
            case R.id.btnYapaAudio :
                return Yapa.TYPE_AUDIO;
            case R.id.btnYapaUrl :
                return Yapa.TYPE_URL;
            case R.id.btnYapaVideo :
                return Yapa.TYPE_VIDEO;
            case R.id.btnYapaCoupon :
                return Yapa.TYPE_COUPON;
            */
            default :
                throw new AssertionError("Invalid yapa button " + selectedType);
        }
    }
}
