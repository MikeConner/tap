package co.tapdatapp.tapandroid.localdata;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Tag  extends BaseDAO implements SingleTable {

    public final static String NEW_TAG_ID = "=NEW=TAG=";

    private final static String TABLE = "tags";
    private final static String TAG_ID = "tag_id";
    private final static String NAME = "name";
    private final static String CURRENCY = "currency_id";

    private String tagId;
    private String name;
    private int currencyId;
    private Yapa[] yapa = null;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE + " ( " +
                TAG_ID + " VARCHAR(255) PRIMARY KEY, " +
                NAME + " VARCHAR(255) NOT NULL, " +
                CURRENCY + " INT NOT NULL " +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No upgrades to do
    }

    /**
     * @return number of tags in the database
     */
    public int getCount() {
        return getRecordCount(TABLE);
    }

    /**
     * Get a new tag object ordered by alpha order of name
     *
     * @param position The position in the order
     * @return populated Tag object
     */
    public Tag getByOrder(int position) {
        Tag rv = new Tag();
        rv.moveToByOrder(position);
        return rv;
    }

    /**
     * Load this tag's values with the values found at the specified
     * position.
     *
     * @param position position in alphabetical order
     */
    public void moveToByOrder(int position) {
        Cursor c = null;
        try {
            SQLiteDatabase db = getDatabaseHelper().getReadableDatabase();
            c = db.query(
                TABLE,
                new String[]{ TAG_ID, NAME, CURRENCY },
                null, null,
                null, null,
                NAME + " ASC", position + ", 1"
            );
            if (c.moveToFirst()) {
                tagId = c.getString(0);
                name = c.getString(1);
                currencyId = c.getInt(2);
            } else {
                throw new Error("No Tag at position " + position);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Set this object to point to the specified tag
     *
     * @param tagId Tag ID to load
     */
    public void moveTo(String tagId) {
        Cursor c = null;
        try {
            SQLiteDatabase db = getDatabaseHelper().getReadableDatabase();
            c = db.query(
                TABLE,
                new String[]{ NAME, CURRENCY },
                TAG_ID + " = ?",
                new String[]{ tagId },
                null, null,
                null, null
            );
            if (c.moveToFirst()) {
                this.tagId = tagId;
                name = c.getString(0);
                currencyId = c.getInt(1);
            } else {
                throw new Error("No Tag with ID " + tagId);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Update the SQL with the data in this object. Does NOT update
     * or otherwise modify dependent Yapa.
     */
    public void update() {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CURRENCY, currencyId);
        values.put(NAME, name);
        int updated = db.update(
            TABLE,
            values,
            TAG_ID + " = ?",
            new String[] { tagId }
        );
        if (updated != 1) {
            throw new AssertionError("Updated " + updated + " rows");
        }
    }

    /**
     * Create a tag in the database from the provided information
     *
     * @param id Tag ID
     * @param name Name of the tag
     * @param currency Currency ID of the tag
     * @param y List of Yapa associated with this tag
     */
    public void
    create(String id, String name, int currency, Yapa[] y) {
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TAG_ID, id);
        values.put(NAME, name);
        values.put(CURRENCY, currency);
        db.insert(TABLE, null, values);
        for (Yapa oneYapa : y) {
            oneYapa.create(id);
        }
    }

    /**
     * Remove all records
     */
    public void removeAll() {
        Yapa y = new Yapa();
        y.removeAll();
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        db.delete(TABLE, null, null);
    }

    /**
     * Remove all records associated with the passed ID
     */
    public void remove(String tagId) {
        Yapa y = new Yapa();
        y.remove(tagId);
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        db.delete(TABLE, TAG_ID + " = ?", new String[] {tagId});
    }

    /**
     * Get the next highest threshold that's not in use
     */
    public int getNextAvailableThreshold() {
        return new Yapa().getNextAvailableThreshold(tagId);
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    /**
     * Lazy load yapa objects for this tag
     *
     * @return Array of Yapa objects
     */
    public synchronized Yapa[] getYapa() {
        if (yapa == null) {
            yapa = new Yapa().getAllForTag(tagId);
        }
        return yapa;
    }

    public void setYapa(Yapa[] y) {
        yapa = y;
    }
}
