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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.NoSuchElementException;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.currency.BalanceList;
import co.tapdatapp.tapandroid.helpers.DateTime;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.remotedata.CurrencyCodec;
import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.WebServiceError;

public class CurrencyDAO
extends BaseDAO
implements SingleTable {

    public final static int CURRENCY_BITCOIN = -1;

    public final static String TABLE = "currency";
    public final static String ID = "_id";
    public final static String NAME = "name";
    public final static String ICON = "icon";
    public final static String SYMBOL = "symbol";
    public final static String MAX_TAP = "max_tap";
    public final static String OWNED = "owned";
    public final static String LAST_UPDATE = "last_update";

    private final static String BITCOIN_NAME = "Bitcoin";
    private final static String BITCOIN_ICON = "http://example.com/nope";
    private final static String BITCOIN_SYMBOL = "S";
    private final static int BITCOIN_MAX_TAP = 500;

    /**
     * Time at which currency details must be refreshed from the server
     * (24 hours)
     */
    private final static long EXPIRATION = 60 * 60 * 24;

    private int currencyId;
    private String name;
    private String icon;
    private String symbol;
    // @TODO make something check this to ensure it's not exceeded
    private int maxTap;
    private long lastUpdate;
    private boolean owned;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE + " ( " +
                ID + " INT PRIMARY KEY, " +
                NAME + " TEXT NOT NULL, " +
                ICON + " TEXT NOT NULL, " +
                SYMBOL + " TEXT NOT NULL, " +
                MAX_TAP + " INT NOT NULL," +
                OWNED + " INT NOT NULL DEFAULT 0," +
                LAST_UPDATE + " BIGINT NOT NULL" +
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
     * @param maxTap Maximum amount of a single transaction
     * @param ownedByMe true if this user owns this currency
     */
    public void createOrUpdate(int id,
                               String name,
                               String icon,
                               String symbol,
                               int maxTap,
                               boolean ownedByMe
    ) {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        ContentValues v = new ContentValues();
        v.put(ID, id);
        v.put(NAME, name);
        v.put(ICON, icon);
        v.put(SYMBOL, symbol);
        v.put(MAX_TAP, maxTap);
        v.put(OWNED, ownedByMe ? 1 : 0);
        v.put(LAST_UPDATE, DateTime.currentEpochTime());
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
                                  boolean ownedByMe,
                                  Denomination[] denominations
    ) {
        createOrUpdate(id, name, icon, symbol, maxTap, ownedByMe);
        updateAllDenominations(id, denominations);
    }

    /**
     * Does the legwork of moveToByOrder() but does not ensure the currency
     * is already loaded, thus preventing endless loops in
     * ensureLocalCurrencyDetails()
     *
     * @param id currency ID
     */
    public void moveTo(int id) {
        Cursor c = null;
        try {
            c = getCursor(
                TABLE,
                new String[]{NAME, ICON, SYMBOL, MAX_TAP, OWNED, LAST_UPDATE},
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
            owned = c.getInt(4) == 1;
            lastUpdate = c.getLong(5);
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public CurrencyDAO[] getAllByNameOrder() {
        Cursor c = null;
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        try {
            c = db.query(
                TABLE,
                new String[] { ID, NAME, ICON, SYMBOL, MAX_TAP, OWNED, LAST_UPDATE },
                null, null,
                null, null,
                NAME + " ASC",
                null
            );
            CurrencyDAO rv[] = new CurrencyDAO[c.getCount()];
            for (int i = 0; i < rv.length; i++) {
                CurrencyDAO ob = new CurrencyDAO();
                if (!c.moveToPosition(i)) {
                    throw new AssertionError("position " + i + " not valid");
                }
                ob.currencyId = c.getInt(0);
                ob.name = c.getString(1);
                ob.icon = c.getString(2);
                ob.symbol = c.getString(3);
                ob.maxTap = c.getInt(4);
                ob.owned = c.getInt(5) == 1;
                ob.lastUpdate = c.getLong(6);
                rv[i] = ob;
            }
            return rv;
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public Bitmap getIcon() throws Exception {
        return TapBitmap.fetchFromCacheOrWeb(getIconUrl());
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getIconUrl() {
        return icon;
    }

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
    public BalanceList getAllBalances() throws WebServiceError {
        HttpHelper http = new HttpHelper();
        JSONObject response = http.HttpGetJSON(
            http.getFullUrl(R.string.ENDPOINT_GET_BALANCES),
            new Bundle()
        );
        CurrencyCodec cc = new CurrencyCodec();
        try {
            return cc.parseBalances(response);
        }
        catch (JSONException je) {
            throw new WebServiceError(je);
        }
    }

    /**
     * Retrieve the list of currencies owned by this user, and ensure
     * the details are on the phone.
     *
     * @throws WebServiceError
     */
    public void updateAllOwnedCurrencies() throws WebServiceError {
        HttpHelper http = new HttpHelper();
        JSONObject response = http.HttpGetJSON(
            http.getFullUrl(R.string.ENDPOINT_GET_CURRENCY),
            new Bundle()
        );
        int[] rv;
        try {
            JSONArray values = response.getJSONArray("response");
            rv = new int[values.length()];
            for (int i = 0; i < values.length(); i++) {
                rv[i] = values.getInt(i);
            }
        }
        catch (JSONException je) {
            throw new WebServiceError(je);
        }
        for (int item : rv) {
            ensureLocalCurrencyDetails(item);
            setCurrencyOwned(item, true);
        }
    }

    /**
     * Flag the specified currency as either owned or not owned by
     * this user.
     */
    private void setCurrencyOwned(int currencyId, boolean owned) {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(OWNED, owned ? 1 : 0);
        db.update(
            TABLE,
            v,
            ID + " = ?",
            new String[] { Integer.toString(currencyId) }
        );
    }

    /**
     * Ensure that all the details of every currency listed in
     * the provide BalanceList are in the database.
     *
     * @param list list of Currencies to update
     */
    public void
    ensureLocalCurrencyDetails(BalanceList list) throws WebServiceError {
        for (int currencyId : list.keySet()) {
            ensureLocalCurrencyDetails(currencyId);
        }
    }

    public void
    ensureLocalCurrencyDetails(int currencyId)
    throws WebServiceError {
        if (currencyId == CURRENCY_BITCOIN) {
            createOrUpdateAll(
                CURRENCY_BITCOIN,
                BITCOIN_NAME,
                BITCOIN_ICON,
                BITCOIN_SYMBOL,
                BITCOIN_MAX_TAP,
                true,
                null
            );
            return;
        }
        try {
            moveTo(currencyId);
            if (lastUpdate > (DateTime.currentEpochTime() + EXPIRATION)) {
                syncCurrencyWithServer(currencyId);
            }
        }
        catch (NoSuchElementException nsee) {
            // Exception indicates that the currency isn't stored
            // locally, must fetch it remotely.
            syncCurrencyWithServer(currencyId);
        }
    }

    private void
    syncCurrencyWithServer(int currencyId)
    throws WebServiceError {
        HttpHelper http = new HttpHelper();
        CurrencyCodec cc = new CurrencyCodec();
        JSONObject response = http.HttpGetJSON(
            getCurrencyURL(http, currencyId),
            new Bundle()
        );
        try {
            cc.parse(currencyId, response);
        }
        catch (JSONException je) {
            throw new WebServiceError(je);
        }
        createOrUpdateAll(
            cc.getId(),
            cc.getName(),
            cc.getIcon(),
            cc.getSymbol(),
            cc.getMaxTap(),
            false,
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
        sb.append(".json");
        http.appendAuthTokenIfExists(sb);
        return sb.toString();
    }

    /**
     * Return all currencies owned by this user (always includes
     * bitcoin, see ensureLocalCurrencyDetails())
     *
     * Uses LinkedHashMap to ensure that the order is consistent
     */
    public LinkedHashMap<Integer, String> getAllOwnedCurrencies() {
        LinkedHashMap<Integer, String> rv = new LinkedHashMap<>();
        CurrencyDAO[] fullList = getAllByNameOrder();
        for (CurrencyDAO one : fullList) {
            if (one.owned) {
                rv.put(one.currencyId, one.name);
            }
        }
        return rv;
    }
}
