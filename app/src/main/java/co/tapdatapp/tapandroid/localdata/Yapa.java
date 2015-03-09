package co.tapdatapp.tapandroid.localdata;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.UUID;

public class Yapa extends BaseDAO implements SingleTable {

    public static final String TYPE_IMAGE = "image";
    public static final String TYPE_URL = "url";
    public static final String TYPE_TEXT = "text";
    public static final String TYPE_VIDEO = "video";
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_COUPON = "coupon";

    private final static String TABLE = "yapa";
    private final static String TAG_ID = "tag_id";
    private final static String THRESHOLD = "threshold";
    private final static String CONTENT = "content";
    private final static String IMAGE = "image";
    private final static String THUMB = "thumb";
    private final static String SLUG = "slug";
    private final static String TYPE = "type";

    private String tagId;
    private String content;
    private int threshold;
    private String thumb;
    private String image;
    private UUID slug;
    private String type;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE + " ( " +
                TAG_ID + " VARCHAR(255) NOT NULL, " +
                THRESHOLD + " INT NOT NULL, " +
                CONTENT + " VARCHAR(255), " +
                IMAGE + " VARCHAR(255), " +
                THUMB + " VARCHAR(255), " +
                SLUG + " VARCHAR(36) NOT NULL UNIQUE, " +
                TYPE + " VARCHAR(50) NOT NULL," +
                "PRIMARY KEY(" + TAG_ID + "," + THRESHOLD + ") " +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * Create a new yapa record with the data in this object
     */
    public void create(String _tagId) {
        tagId = _tagId;
        create();
    }

    public void create() {
        if (slug == null) {
            throw new NullPointerException("Attempt to create with null slug");
        }
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TAG_ID, tagId);
        values.put(THRESHOLD, threshold);
        values.put(CONTENT, content);
        values.put(IMAGE, image);
        values.put(THUMB, thumb);
        values.put(SLUG, slug.toString());
        values.put(TYPE, type);
        db.insert(TABLE, null, values);
    }

    /**
     * Remove all records
     */
    public void removeAll() {
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        db.delete(TABLE, null, null);
    }

    /**
     * Remove all records associated with the passed Tag ID
     */
    public void remove(String tagId) {
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        db.delete(TABLE, TAG_ID + " = ?", new String[] {tagId});
    }

    /**
     * Get the next highest threshold for the passed ID
     */
    public int getNextAvailableThreshold(String id) {
        Cursor c = null;
        try {
            SQLiteDatabase db = getDatabaseHelper().getReadableDatabase();
            c = db.rawQuery(
                "SELECT MAX(" + THRESHOLD + ") FROM " + TABLE +
                    " WHERE " + TAG_ID + " = ?",
                new String[] {id}
            );
            if (c.moveToFirst()) {
                return c.getInt(0) + 1;
            } else {
                return 1;
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Get an array of all the Yapa for the passed Tag ID
     */
    public Yapa[] getAllForTag(String id) {
        Cursor c = null;
        try {
            SQLiteDatabase db = getDatabaseHelper().getReadableDatabase();
            c = db.query(
                TABLE,
                new String[]{THRESHOLD, CONTENT, IMAGE, THUMB, SLUG, TYPE},
                TAG_ID + " = ?",
                new String[]{id},
                null, null,
                THRESHOLD + " ASC", null
            );
            Yapa[] rv = new Yapa[c.getCount()];
            for (int r = 0; r < rv.length; r++) {
                c.moveToPosition(r);
                Yapa y = new Yapa();
                y.setThreshold(c.getInt(0));
                y.setContent(c.getString(1));
                y.setImage(c.getString(2));
                y.setThumb(c.getString(3));
                y.setSlug(UUID.fromString(c.getString(4)));
                y.setTagId(id);
                y.setType(c.getString(5));
                rv[r] = y;
            }
            return rv;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setSlug(UUID slug) {
        this.slug = slug;
    }

    public UUID getSlug() {
        if (slug == null) {
            throw new NullPointerException("null slug");
        }
        return slug;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
