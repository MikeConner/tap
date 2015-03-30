/**
 * Select the type of tag to create, then proceed to the tag management
 * screen.
 */

package co.tapdatapp.tapandroid.tags;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.LinkedHashMap;
import java.util.UUID;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.localdata.Tag;
import co.tapdatapp.tapandroid.localdata.Yapa;

public class SelectTagTypeActivity extends Activity {

    private int[] currencyDropdownMap;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_select_tag_type);
    }

    @Override
    public void onResume() {
        super.onResume();
        fillInCurrencies();
    }

    /**
     * Triggered when a yapa type is selected
     *
     * @param v the button that was selected
     */
    public void onTypeSelect(View v) {
        createTag(v.getId());
    }

    /**
     * Fill in the spinner with the list of available currencies and
     * enable it if the list is longer than 1
     */
    private void fillInCurrencies() {
        CurrencyDAO dao = new CurrencyDAO();
        LinkedHashMap<Integer, String> currencyList = dao.getAllOwnedCurrencies();
        currencyDropdownMap = new int[currencyList.size()];
        String[] currencyArray = new String[currencyList.size()];
        int i = 0;
        for (Integer id : currencyList.keySet()) {
            currencyDropdownMap[i] = id;
            currencyArray[i] = currencyList.get(id);
            i++;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            currencyArray
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ((Spinner)findViewById(R.id.spinnerSelectCurrency)).setAdapter(adapter);
    }

    /**
     * Convert the index in the currency spinner to a currency ID
     */
    private int getSelectedCurrencyId() {
        int itemNumber = (int)((Spinner)findViewById(R.id.spinnerSelectCurrency)).getSelectedItemId();
        return currencyDropdownMap[itemNumber];
    }

    /**
     * Create a new tag in SQL and proceed to the management screen.
     */
    public void createTag(int selectedType) {
        Tag tag = new Tag();
        tag.remove(Tag.NEW_TAG_ID);
        Yapa yapa = new Yapa();
        yapa.setTagId(Tag.NEW_TAG_ID);
        yapa.setThreshold(1);
        yapa.setSlug(UUID.randomUUID());
        yapa.setType(getTypeStringFromId(selectedType));
        tag.create(
            Tag.NEW_TAG_ID,
            "New Tag",
            getSelectedCurrencyId(),
            new Yapa[]{yapa}
        );
        Intent i = new Intent(this, ManageTagActivity.class);
        i.putExtra(ManageTagActivity.MODE, ManageTagActivity.MODE_NEW);
        i.putExtra(ManageTagActivity.TAG_ID, Tag.NEW_TAG_ID);
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
    private String getTypeStringFromId(int selectedType) {
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
