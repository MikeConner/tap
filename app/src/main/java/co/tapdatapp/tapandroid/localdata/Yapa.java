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
    private final static String DESCRIPTION = "description";
    private final static String IMAGE = "image";
    private final static String THUMB = "thumb";
    private final static String SLUG = "slug";
    private final static String TYPE = "type";
    private final static String URI = "uri";

    private String tagId;
    private String content;
    private String description;
    private int threshold;
    private String thumb;
    private String image;
    private UUID slug;
    private String type;
    private String uri;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE + " ( " +
                TAG_ID + " VARCHAR(255) NOT NULL, " +
                THRESHOLD + " INT NOT NULL, " +
                CONTENT + " TEXT, " +
                DESCRIPTION + " VARCHAR(255), " +
                IMAGE + " VARCHAR(255), " +
                THUMB + " VARCHAR(255), " +
                SLUG + " VARCHAR(36) NOT NULL UNIQUE, " +
                TYPE + " VARCHAR(50) NOT NULL," +
                URI + " VARCHAR(255), " +
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
        if (type == null || type.isEmpty()) {
            throw new NullPointerException("Attempt to create with no type");
        }
        SQLiteDatabase db = getDatabaseHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TAG_ID, tagId);
        values.put(THRESHOLD, threshold);
        values.put(CONTENT, content);
        values.put(DESCRIPTION, description);
        values.put(IMAGE, image);
        values.put(THUMB, thumb);
        values.put(SLUG, slug.toString());
        values.put(TYPE, type);
        values.put(URI, uri);
        db.insert(TABLE, null, values);
    }

    /**
     * Update the database with the information in this object
     */
    public void update() {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(THRESHOLD, threshold);
        values.put(CONTENT, content);
        values.put(DESCRIPTION, description);
        values.put(IMAGE, image);
        values.put(THUMB, thumb);
        values.put(TYPE, type);
        values.put(URI, uri);
        int updated = db.update(
            TABLE,
            values,
            SLUG + " = ?",
            new String[] { slug.toString() }
        );
        if (updated != 1) {
            throw new AssertionError("Updated " + updated + " rows");
        }
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
     * Get an array of all the Yapa for the passed Tag ID
     */
    public Yapa[] getAllForTag(String id) {
        Cursor c = null;
        try {
            SQLiteDatabase db = getDatabaseHelper().getReadableDatabase();
            c = db.query(
                TABLE,
                new String[] {
                    THRESHOLD, CONTENT, IMAGE, THUMB, SLUG, TYPE, URI,
                    DESCRIPTION
                },
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
                y.uri = c.getString(6);
                y.description = c.getString(7);
                rv[r] = y;
            }
            return rv;
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getContent() {
        return content;
    }

    /**
     * Sets the content and returns true if the new value differs
     * from the old.
     */
    public boolean setContentIfChanged(String to) {
        boolean rv;
        if (to == null) {
            rv = content != null;
        }
        else {
            rv = !to.equals(content);
        }
        content = to;
        return rv;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getThreshold() {
        return threshold;
    }

    /**
     * Sets the threshold and returns true if the new value differs
     * from the old.
     */
    public boolean setThresholdIfChanged(int to) {
        boolean rv = to != threshold;
        threshold = to;
        return rv;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public String getThumb() {
        return thumb;
    }

    /**
     * Sets the thumbnail and returns true if the new value differs
     * from the old.
     */
    public boolean setThumbIfChanged(String to) {
        boolean rv;
        if (to == null) {
            rv = thumb != null;
        }
        else {
            rv = !to.equals(thumb);
        }
        thumb = to;
        return rv;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getImage() {
        return image;
    }

    /**
     * Sets the image and returns true if the new value differs
     * from the old.
     */
    public boolean setImageIfChanged(String to) {
        boolean rv;
        if (to == null) {
            rv = image != null;
        }
        else {
            rv = !to.equals(image);
        }
        image = to;
        return rv;
    }

    public void setImage(String image) {
        this.image = image;
    }

    /**
     * Sets the URI and returns true if the new value differs
     * from the old.
     */
    public boolean setUriIfChanged(String to) {
        boolean rv;
        if (to == null) {
            rv = uri != null;
        }
        else {
            rv = !to.equals(uri);
        }
        uri = to;
        return rv;
    }

    public void setUri(String to) {
        uri = to;
    }

    public String getUri() {
        return uri;
    }

    /**
     * Generate a new, random UUID/slug
     */
    public void generateSlug() {
        slug = UUID.randomUUID();
    }

    public void setSlug(UUID slug) {
        this.slug = slug;
    }

    public String getType() {
        if (type == null || type.isEmpty()) {
            throw new AssertionError("Invalid emtpy Yapa type");
        }
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Sets the description and returns true if the new value differs
     * from the old.
     */
    public boolean setDescriptionIfChanged(String to) {
        boolean rv;
        if (to == null) {
            rv = description != null;
        }
        else {
            rv = !to.equals(description);
        }
        description = to;
        return rv;
    }

    public void setDescription(String to) {
        description = to;
    }
}
