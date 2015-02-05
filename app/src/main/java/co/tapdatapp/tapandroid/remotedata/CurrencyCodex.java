/**
 * Converts currency data between different representations
 */

package co.tapdatapp.tapandroid.remotedata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import co.tapdatapp.tapandroid.localdata.Denomination;

public class CurrencyCodex {
    private int id;
    private String name;
    private String icon;
    private boolean iconProcessing;
    private String symbol;
    private ArrayList<Denomination> denominations;

    private static final String NAME = "name";
    private static final String ICON = "icon";
    private static final String STATUS = "status";
    private static final String STATUS_ACTIVE = "active";
    private static final String ICON_PROCESSING = "icon_processing";
    private static final String SYMBOL = "symbol";
    private static final String DENOMINATIONS = "denominations";
    private static final String DENOMINATION_AMOUNT = "amount";
    private static final String DENOMINATION_ICON = "icon";

    /**
     * Parse a JSON representation of a currency, and load this object
     * with its values.
     *
     * @param json JSON representation of a currency
     */
    public void parse(int currencyId, JSONObject json) throws JSONException {
        id = currencyId;
        name = json.getString(NAME);
        if (!json.getString(STATUS).equals(STATUS_ACTIVE)) {
            // @TODO this should probably be some other exception, but
            // I'm unclear as to what the various status will mean and
            // how they should be handled
            throw new AssertionError(
                "Currency " + name + " has non-active status"
            );
        }
        icon = json.getString(ICON);
        iconProcessing = json.getBoolean(ICON_PROCESSING);
        symbol = json.getString(SYMBOL);
        JSONArray jsonDenominations = json.getJSONArray(DENOMINATIONS);
        parseDenominations(jsonDenominations);
    }

    /**
     * This feels wrong, having denominations parsed in the Currency
     * codex, but they are so tightly coupled that it's probably not
     * an issue.
     *
     * @param json JSONArray listing all denominations
     */
    public void parseDenominations(JSONArray json) throws JSONException {
        denominations = new ArrayList<Denomination>();
        for (int i = 0; i < json.length(); i++) {
            denominations.add(parseDenomination(json.getJSONObject(i)));
        }
    }

    /**
     * Parse a JSON object containing a single denomination and return
     * a denomination object.
     *
     * @param json containing a single denomination
     * @return Denomination object
     */
    public Denomination parseDenomination(JSONObject json)
    throws JSONException {
        return new Denomination(
            id,
            json.getInt(DENOMINATION_AMOUNT),
            json.getString(DENOMINATION_ICON)
        );
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public boolean getIconProcessing() {
        return iconProcessing;
    }

    public String getSymbol() {
        return symbol;
    }

    public List<Denomination> getDenominations() {
        return denominations;
    }
}
