package co.tapdatapp.tapandroid.localdata;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import co.tapdatapp.tapandroid.remotedata.TagCodec;
import co.tapdatapp.tapandroid.remotedata.YapaCodec;

public class Tag implements SingleTable {

    private final static String TABLE = "tags";
    private final static String TAG_ID = "tag_id";
    private final static String NAME = "name";

    private String tagId;
    private String name;
    private ArrayList<Yapa> yapa = null;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE + " ( " +
                TAG_ID + " VARCHAR(255) PRIMARY KEY, " +
                NAME + " VARCHAR(255) NOT NULL " +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No upgrades to do
    }

    /**
     * Set this object to point to the specified tag
     *
     * @param tagId Tag ID to load
     */
    public void moveTo(String tagId) {
        Cursor c = null;
        try {
            SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
            c = db.query(
                TABLE,
                new String[]{ NAME },
                TAG_ID + " = ?",
                new String[]{ tagId },
                null, null,
                null, null
            );
            if (c.moveToFirst()) {
                this.tagId = tagId;
                name = c.getString(0);
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
     * Returns a new Tag object
     *
     * @param id The tag ID of the object to return
     * @return A Tag object pointing to id
     */
    public Tag getTag(String id) {
        Tag tag = new Tag();
        tag.moveTo(id);
        return tag;
    }

    /**
     * Create a tag in the database from the data in the provided codec
     *
     * @param codec TagCodec object with parse() having been called
     */
    public void create(TagCodec codec, YapaCodec yapa) {
        //create(codec.getId(), codec.getName(), yapa.getYapaList());
    }

    public void create(String id, String name, Yapa[] yapa) {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TAG_ID, id);
        values.put(NAME, name);
        db.insert(TABLE, null, values);
        for (Yapa y : yapa) {
            y.create(id);
        }
    }

    /**
     * Remove all records
     */
    public void removeAll() {
        Yapa yapa = new Yapa();
        yapa.removeAll();
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getWritableDatabase();
        db.delete(TABLE, null, null);
    }
}
