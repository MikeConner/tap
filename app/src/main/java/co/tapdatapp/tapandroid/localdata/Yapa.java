package co.tapdatapp.tapandroid.localdata;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

public class Yapa implements SingleTable {

    private final static String TABLE = "yapa";
    private final static String TAG_ID = "tag_id";
    private final static String THRESHOLD = "threshold";
    private final static String CONTENT = "content";
    private final static String IMAGE = "image";
    private final static String THUMB = "thumb";

    private String tagId;
    private String content;
    private int threshold;
    private String thumb;
    private String image;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE + " ( " +
                TAG_ID + " VARCHAR(255) NOT NULL, " +
                THRESHOLD + " INT NOT NULL, " +
                CONTENT + " VARCHAR(255), " +
                IMAGE + " VARCHAR(255), " +
                THUMB + " VARCHAR(255), " +
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
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TAG_ID, tagId);
        values.put(THRESHOLD, threshold);
        values.put(CONTENT, content);
        values.put(IMAGE, image);
        values.put(THUMB, thumb);
        db.insert(TABLE, null, values);
    }

    /**
     * Remove all records
     */
    public void removeAll() {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getWritableDatabase();
        db.delete(TABLE, null, null);
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
}
