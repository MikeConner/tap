/**
 * Converts currency data between different representations
 */

package co.tapdatapp.tapandroid.remotedata;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import co.tapdatapp.tapandroid.currency.BalanceList;
import co.tapdatapp.tapandroid.localdata.BaseCodec;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.localdata.Denomination;

public class CurrencyCodec extends BaseCodec {
    private int id;
    private String name;
    private String icon;
    private String symbol;
    private int maxTap;
    private ArrayList<Denomination> denominations;

    private static final String NAME = "name";
    private static final String ICON = "icon";
    private static final String SYMBOL = "symbol";
    private static final String MAX_TAP = "max_amount";
    private static final String DENOMINATIONS = "denominations";
    private static final String DENOMINATION_AMOUNT = "amount";
    private static final String DENOMINATION_ICON = "image";

    /**
     * Parse a JSON representation of a currency, and load this object
     * with its values.
     *
     * @param json JSON representation of a currency
     */
    public void parse(int currencyId, JSONObject json) throws JSONException {
        id = currencyId;
        JSONObject nest = json.getJSONObject("response");
        name = nest.getString(NAME);
        icon = nest.getString(ICON);
        symbol = ifNull(nest.getString(SYMBOL), "");
        maxTap = nest.getInt(MAX_TAP);
        JSONArray jsonDenominations = nest.getJSONArray(DENOMINATIONS);
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
        denominations = new ArrayList<>();
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

    public BalanceList
    parseBalances(JSONObject response) throws JSONException {
        JSONObject in = response.getJSONObject("response");
        BalanceList rv = new BalanceList();
        rv.put(CurrencyDAO.CURRENCY_BITCOIN, in.getInt("dollar_balance"));
        JSONArray balances = in.getJSONArray("balances");
        for (int i = 0; i < balances.length(); i++) {
            JSONObject oneBalance = balances.getJSONObject(i);
            rv.put(oneBalance.getInt("id"), oneBalance.getInt("amount"));
        }
        return rv;
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

    public String getSymbol() {
        return symbol;
    }

    public int getMaxTap() {
        return maxTap;
    }

    public Denomination[] getDenominations() {
        return denominations.toArray(new Denomination[denominations.size()]);
    }
}
