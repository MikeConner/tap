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

import java.util.NoSuchElementException;

public class UserBalance extends BaseDAO implements SingleTable {

    public final static String TABLE = "currency";
    public final static String ID = "_id";
    public final static String NAME = "name";
    public final static String ICON = "icon";
    public final static String SYMBOL = "symbol";

    private String name;
    private String icon;
    private String symbol;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE + " ( " +
                ID + " INT PRIMARY KEY, " +
                NAME + " TEXT NOT NULL, " +
                ICON + " TEXT NOT NULL, " +
                SYMBOL + " TEXT NOT NULL " +
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
                               String symbol
    ) {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        ContentValues v = new ContentValues();
        v.put(ID, id);
        v.put(NAME, name);
        v.put(ICON, icon);
        v.put(SYMBOL, symbol);
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
                                  Denomination[] denominations
    ) {
        createOrUpdate(id, name, icon, symbol);
        updateAllDenominations(id, denominations);
    }

    /**
     * Load this class up with the parameters from the specified
     * currency ID
     *
     * @param id Currency ID
     */
    public void moveTo(int id) {
        Cursor c = null;
        try {
            c = getCursor(
                TABLE,
                new String[] { NAME, ICON, SYMBOL },
                ID + " = ?",
                new String[] { Integer.toString(id) }
            );
            if (c.getCount() != 1) {
                throw new NoSuchElementException(
                    "Records for id " + id + " = " + c.getCount()
                );
            }
            c.moveToFirst();
            name = c.getString(0);
            icon = c.getString(1);
            symbol = c.getString(2);
        }
        finally {
            if (c != null) {
                c.close();
            }
        }

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
}
