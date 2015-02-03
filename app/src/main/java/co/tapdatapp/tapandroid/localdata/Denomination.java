/**
 * Tracks Bitmaps to provide fancy UI images for denominations
 */

package co.tapdatapp.tapandroid.localdata;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import java.util.NoSuchElementException;

import co.tapdatapp.tapandroid.remotedata.HttpHelper;
import co.tapdatapp.tapandroid.remotedata.WebResponse;

public class Denomination extends BaseDAO implements SingleTable {

    public final static String TABLE = "denomination";
    public final static String CURRENCY_ID = "currency";
    public final static String AMOUNT = "amount";
    public final static String IMAGE = "image";

    private int currency_id;
    private int amount;
    private String image;

    public Denomination() {
        super();
    }

    public Denomination(int cid, int a, String i) {
        currency_id = cid;
        amount = a;
        image = i;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE + " ( " +
                CURRENCY_ID + " INT NOT NULL, " +
                AMOUNT + " INT NOT NULL, " +
                IMAGE + " TEXT NOT NULL, " +
                "PRIMARY KEY (" + CURRENCY_ID + ", " + AMOUNT + ")" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No changes
    }

    /**
     * Get the URL of the image for the requested denomination
     *
     * @param currency the currency ID of the desired URL
     * @param amount The amount of the desired URL
     * @return URL as a string for fetching the image
     */
    public String getURL(int currency, int amount) {
        try {
            return getString(
                TABLE,
                IMAGE,
                CURRENCY_ID + " = ? AND " + AMOUNT + " = ?",
                new String[]{
                    Integer.toString(currency),
                    Integer.toString(amount)
                }
            );
        }
        catch (RuntimeException rte) {
            throw new NoSuchElementException(
                "Currency Id = " + currency + ", amount = " + amount
            );
        }
    }

    /**
     * Return the Bitmap of the requested URL.
     *
     * @param currency the currency ID of the desired Denomination
     * @param amount The amount of the denomination
     * @return Bitmap of the requested Denomination icon
     * @throws Exception If the bitmap is not cached and can't be fetched
     */
    public Bitmap getBitmap(int currency, int amount) throws Exception {
        Bitmap rv;
        AndroidCache cache = new AndroidCache();
        String url = getURL(currency, amount);
        byte[] data = cache.get(url);
        if (data == null) {
            HttpHelper helper = new HttpHelper();
            WebResponse wr = helper.HttpGet(url, new Bundle());
            if (wr.isOK()) {
                data = wr.getBody();
                if (data == null) {
                    throw new NullPointerException("Null data from cache");
                }
                if (data.length == 0) {
                    throw new NullPointerException("0 length data from cache");
                }
                rv = BitmapFactory.decodeByteArray(data, 0, data.length);
                if (rv == null) {
                    throw new NullPointerException(
                        "Null bitmap from webservice for " + url
                    );
                }
                cache.put(url, wr.getMediaType(), data);
            }
            else {
                throw new Exception("Failure to fetch image: " + wr.getError());
            }
        }
        else {
            rv = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (rv == null) {
                throw new NullPointerException(
                    "Null Bitmap from cache for " + url + " " + data.length
                );
            }
        }
        return rv;
    }

    /**
     * Create a new Denomination entry with the specified data in this
     * objects members.
     */
    public void create() {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(CURRENCY_ID, currency_id);
        v.put(AMOUNT, amount);
        v.put(IMAGE, image);
        db.insertOrThrow(TABLE, null, v);
    }

    /**
     * Remove all Denominations for the specified currency
     *
     * @param currency currency ID
     */
    public void removeAll(int currency) {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getWritableDatabase();
        db.delete(
            TABLE,
            CURRENCY_ID + " = ?",
            new String[] { Integer.toString(currency)}
        );
    }

}
