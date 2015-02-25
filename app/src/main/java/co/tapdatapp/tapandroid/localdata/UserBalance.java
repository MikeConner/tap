/**
 * Represents both the details of a currency as well as the balance
 * for the current user. On the surface, this seems like multiple
 * responsibilities for a single class, but a user's balance and the
 * associated currency are tightly coupled.
 */

package co.tapdatapp.tapandroid.localdata;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.NoSuchElementException;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.currency.BalanceList;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.remotedata.CurrencyCodec;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.WebServiceError;

public class UserBalance
extends BaseDAO
implements SingleTable, CurrencyDAO {

    public final static String TABLE = "currency";
    public final static String ID = "_id";
    public final static String NAME = "name";
    public final static String ICON = "icon";
    public final static String SYMBOL = "symbol";
    public final static String MAX_TAP = "max_tap";

    private final static String BITCOIN_NAME = "Bitcoin";
    private final static String BITCOIN_ICON = "http://example.com/nope";
    private final static String BITCOIN_SYMBOL = "S";
    private final static int BITCOIN_MAX_TAP = 500;

    private int currencyId;
    private String name;
    private String icon;
    private String symbol;
    private int maxTap;

    // @TODO currency needs to expire after 1 week, store last update time
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE + " ( " +
                ID + " INT PRIMARY KEY, " +
                NAME + " TEXT NOT NULL, " +
                ICON + " TEXT NOT NULL, " +
                SYMBOL + " TEXT NOT NULL, " +
                MAX_TAP + " INT NOT NULL" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db,
                          int oldVersion,
                          int newVersion
    ) {
        // No changes
    }

    /**
     * Create or update only the currency record with the data
     * provided
     *
     * @param id Currency ID
     * @param name Currency name
     * @param icon URL of the icon
     * @param symbol String of the symbol (such as "$")
     */
    public void createOrUpdate(int id,
                               String name,
                               String icon,
                               String symbol,
                               int maxTap
    ) {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        ContentValues v = new ContentValues();
        v.put(ID, id);
        v.put(NAME, name);
        v.put(ICON, icon);
        v.put(SYMBOL, symbol);
        v.put(MAX_TAP, maxTap);
        db.insertWithOnConflict(
            TABLE,
            null,
            v,
            SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    /**
     * Update all denominations for the provided currency ID. If the
     * second parameter is an empty array or null, all denominations
     * will be removed for the specified currency ID.
     *
     * @param id currency ID
     * @param d array of denominations to create/update
     */
    public void updateAllDenominations(int id, Denomination[] d) {
        new Denomination().removeAll(id);
        if (d == null || d.length == 0) {
            return;
        }
        for (Denomination d1 : d) {
            d1.create();
        }
    }

    @Override
    public Denomination[] getDenominations(int currencyId) {
        return new Denomination().getAllForCurrency(currencyId);
    }

    /**
     * Create a new currency with denominations or update and existing
     * one with the provided data. If denominations is null or an
     * empty string, all denominations will be deleted.
     *
     * @param id currency ID
     * @param name currency name
     * @param icon URL to currency icon
     * @param symbol symbol (such as "$")
     * @param denominations Array of Denominations
     */
    public void createOrUpdateAll(int id,
                                  String name,
                                  String icon,
                                  String symbol,
                                  int maxTap,
                                  Denomination[] denominations
    ) {
        createOrUpdate(id, name, icon, symbol, maxTap);
        updateAllDenominations(id, denominations);
    }

    /**
     * Does the legwork of moveTo() but does not ensure the currency
     * is already loaded, thus preventing endless loops in
     * ensureLocalCurrencyDetails()
     *
     * @param id currency ID
     */
    @Override
    public void moveTo(int id) {
        Cursor c = null;
        try {
            c = getCursor(
                TABLE,
                new String[]{NAME, ICON, SYMBOL, MAX_TAP},
                ID + " = ?",
                new String[]{Integer.toString(id)}
            );
            if (c.getCount() != 1) {
                throw new NoSuchElementException(
                    "Records for id " + id + " = " + c.getCount()
                );
            }
            c.moveToFirst();
            currencyId = id;
            name = c.getString(0);
            icon = c.getString(1);
            symbol = c.getString(2);
            maxTap = c.getInt(3);
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }

    @Override
    public CurrencyDAO getByNameOrder(int index) {
        Cursor c = null;
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        try {
            c = db.query(
                TABLE,
                new String[] { ID, NAME, ICON, SYMBOL, MAX_TAP },
                null, null,
                null, null,
                NAME + " ASC",
                index + ", 1"
            );
            if (c.getCount() != 1) {
                throw new NoSuchElementException(
                    "No record at location " + index
                );
            }
            c.moveToFirst();
            UserBalance rv = new UserBalance();
            rv.currencyId = c.getInt(0);
            rv.name = c.getString(1);
            rv.icon = c.getString(2);
            rv.symbol = c.getString(3);
            rv.maxTap = c.getInt(4);
            return rv;
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }

    @Override
    public Bitmap getIcon() throws Exception {
        return TapBitmap.fetchFromCacheOrWeb(getIconUrl());
    }

    @Override
    public int getCurrencyId() {
        return currencyId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public String getSymbol(int currencyId) {
        moveTo(currencyId);
        return getSymbol();
    }

    @Override
    public String getIconUrl() {
        return icon;
    }

    @Override
    public int getBalance(int currencyId) throws WebServiceError {
        BalanceList balances = getAllBalances();
        if (balances.containsKey(currencyId)) {
            return balances.get(currencyId);
        }
        else {
            throw new NoSuchElementException(
                "No balance for currency " + currencyId
            );
        }
    }

    @Override
    public String
    getBalanceAsString(int currencyId) throws WebServiceError {
        moveTo(currencyId);
        return getSymbol() + Integer.toString(getBalance(currencyId));
    }

    /**
     * This implementation always fetches balances from the webservice
     *
     * @return List of currencies and balances
     */
    @Override
    public BalanceList getAllBalances() throws WebServiceError {
        HttpHelper http = new HttpHelper();
        JSONObject response = http.HttpGetJSON(
            http.getFullUrl(R.string.ENDPOINT_GET_BALANCES),
            new Bundle()
        );
        CurrencyCodec cc = new CurrencyCodec();
        BalanceList rv = null;
        try {
            rv = cc.parseBalances(response);
        }
        catch (JSONException je) {
            TapApplication.unknownFailure(je);
        }
        return rv;
    }

    /**
     * Ensure that all the details of every currency listed in
     * the provide BalanceList are in the database.
     *
     * @param list list of Currencies to update
     */
    public void ensureLocalCurrencyDetails(BalanceList list) {
        for (int currencyId : list.keySet()) {
            ensureLocalCurrencyDetails(currencyId);
        }
    }

    // @TODO needs an expiration time on the currency to refresh
    @Override
    public void ensureLocalCurrencyDetails(int currencyId) {
        if (currencyId == CURRENCY_BITCOIN) {
            createOrUpdateAll(
                CURRENCY_BITCOIN,
                BITCOIN_NAME,
                BITCOIN_ICON,
                BITCOIN_SYMBOL,
                BITCOIN_MAX_TAP,
                null
            );
            return;
        }
        try {
            moveTo(currencyId);
            // If this succeeds, the currency is already local,
            // nothing else needs to be done
        }
        catch (NoSuchElementException nsee) {
            // Exception indicates that the currency isn't stored
            // locally, must fetch it remotely.
            try {
                syncCurrencyWithServer(currencyId);
            }
            catch (Exception wse) {
                TapApplication.unknownFailure(wse);
            }
        }
    }

    @Override
    public int getMaxPayout(int currencyId) {
        moveTo(currencyId);
        return maxTap;
    }

    private void
    syncCurrencyWithServer(int currencyId)
    throws WebServiceError, JSONException {
        HttpHelper http = new HttpHelper();
        CurrencyCodec cc = new CurrencyCodec();
        JSONObject response = http.HttpGetJSON(
            getCurrencyURL(http, currencyId),
            new Bundle()
        );
        cc.parse(currencyId, response);
        createOrUpdateAll(
            cc.getId(),
            cc.getName(),
            cc.getIcon(),
            cc.getSymbol(),
            cc.getMaxTap(),
            cc.getDenominations()
        );
    }

    /**
     * Again, some duplication with HttpHelper.getFullUrl()
     *
     * @param http HttpHelper
     * @param currencyId curerncy ID to append to the URL
     * @return String URL to the resource
     */
    // @TODO unify this into HttpHelper
    private String getCurrencyURL(HttpHelper http, int currencyId) {
        StringBuilder sb = new StringBuilder();
        sb.append(TapApplication.string(R.string.SERVER));
        sb.append(TapApplication.string(R.string.API_VERSION));
        sb.append(TapApplication.string(R.string.ENDPOINT_GET_CURRENCY));
        sb.append("/");
        sb.append(Integer.toString(currencyId));
        http.appendAuthTokenIfExists(sb);
        Log.d("CURRENCY_REMOTE", sb.toString());
        return sb.toString();
    }

}
