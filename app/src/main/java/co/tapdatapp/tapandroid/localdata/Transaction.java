/**
 * Data object wrapping a Transaction
 */

package co.tapdatapp.tapandroid.localdata;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Timestamp;

public class Transaction implements SingleTable {
    public final static String NAME = "transactions";
    public final static String SLUG = "slug";
    public final static String TIMESTAMP = "timestamp";
    public final static String THUMB_URL = "thumb_url";
    public final static String YAPA_THUMB_URL = "yapa_thumb_url";
    public final static String YAPA_URL = "yapa_url";
    public final static String DESCRIPTION = "description";
    public final static String AMOUNT = "amount";
    public final static String NICKNAME = "nickname";
    public final static String YAPA_CONTENT_TYPE = "yapa_content_type";
    public final static String CONTENT = "content";
    public final static String URI = "uri";
    public final static String TAG_NAME = "tag_name";

    private String slug;
    private Timestamp timestamp;
    private String thumb_url;
    private String yapa_thumb_url;
    private String yapa_url;
    private String description;
    private int amount;
    private String nickname;
    private String yapa_content_type;
    private String content;
    private String uri;
    private String tag_name;

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + NAME + " ( " +
                SLUG + " TEXT PRIMARY KEY, " +
                TIMESTAMP + " BIGINT NOT NULL, " +
                THUMB_URL + " TEXT NOT NULL, " +
                YAPA_THUMB_URL + " TEXT, " +
                YAPA_URL + " TEXT, " +
                DESCRIPTION + " TEXT NOT NULL, " +
                AMOUNT + " DECIMAL NOT NULL, " +
                NICKNAME + " TEXT NOT NULL, " +
                YAPA_CONTENT_TYPE + " TEXT NOT NULL," +
                CONTENT + " TEXT, " +
                URI + " TEXT, " +
                TAG_NAME + " TEXT" +
                ")"
        );
    }

    @Override
    public void onUpgrade(
        SQLiteDatabase sqLiteDatabase,
        int oldVersion,
        int newVersion
    ) {
        throw new UnsupportedOperationException("No upgrades to do");
    }

    /**
     * Get the number of transaction records in the database
     */
    public int getRecordCount() {
        Cursor c = null;
        try {
            SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
            c = db.rawQuery("SELECT count(*) FROM " + NAME, null);
            if (c.moveToFirst()) {
                return c.getInt(0);
            } else {
                throw new Error("Unexplained failure to count transactions");
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    public Timestamp getNewest() {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery(
                "SELECT MAX(" + TIMESTAMP + ") FROM " + NAME,
                null,
                null
            );
            if (c.moveToFirst()) {
                return new Timestamp(c.getLong(0));
            } else {
                throw new RuntimeException("Rows returned = " + c.getCount());
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Return a new transaction object populated with the data at
     * the specified location, ordered by descending timestamp.
     */
    public Transaction getByOrder(int location) {
        Transaction t = new Transaction();
        t.moveToByOrder(location);
        return t;
    }

    /**
     * Load this object with data from the record at the specified
     * location, order by timestamp descending.
     */
    public void moveToByOrder(int location) {
        Cursor c = null;
        try {
            SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
            c = db.query(
                NAME,
                new String[]{
                    SLUG, TIMESTAMP, THUMB_URL, YAPA_URL,
                    DESCRIPTION, AMOUNT, NICKNAME,
                    YAPA_THUMB_URL, YAPA_CONTENT_TYPE, CONTENT, URI,
                    TAG_NAME
                },
                null, null, null, null,
                TIMESTAMP + " DESC",
                location + ", 1"
            );
            loadFromCursor(c);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Move to the record with the supplied slug
     *
     * @param slug The unique ID for the transaction
     */
    public void moveToSlug(String slug) {
        Cursor c = null;
        try {
            SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
            c = db.query(
                NAME,
                new String[]{
                    SLUG, TIMESTAMP, THUMB_URL, YAPA_URL,
                    DESCRIPTION, AMOUNT, NICKNAME,
                    YAPA_THUMB_URL, YAPA_CONTENT_TYPE, CONTENT, URI,
                    TAG_NAME
                },
                SLUG + " = ?",
                new String[] { slug },
                null, null,
                null, null
            );
            loadFromCursor(c);
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    private void loadFromCursor(Cursor c) {
        if (c.moveToFirst()) {
            slug = c.getString(0);
            timestamp = new Timestamp(c.getLong(1));
            thumb_url = c.getString(2);
            yapa_url = c.getString(3);
            description = c.getString(4);
            amount = c.getInt(5);
            nickname = c.getString(6);
            yapa_thumb_url = c.getString(7);
            yapa_content_type = c.getString(8);
            content = c.getString(9);
            uri = c.getString(10);
            tag_name = c.getString(11);
        } else {
            throw new Error("No TX record found");
        }
    }

    /**
     * Create a new transaction record in the database with the data
     * currently stored in this object.
     */
    public void create() {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        ContentValues v = new ContentValues();
        v.put(SLUG, slug);
        v.put(TIMESTAMP, timestamp.getTime());
        v.put(THUMB_URL, thumb_url);
        v.put(YAPA_URL, yapa_url);
        v.put(DESCRIPTION, description);
        v.put(AMOUNT, amount);
        v.put(NICKNAME, nickname);
        v.put(YAPA_THUMB_URL, yapa_thumb_url);
        v.put(YAPA_CONTENT_TYPE, yapa_content_type);
        v.put(CONTENT, content);
        v.put(URI, uri);
        v.put(TAG_NAME, tag_name);
        // Sometimes the webservice returns the same transaction even
        // though it's been told to only return newer. This is probably
        // the result of precision lost in the date as it passes from
        // java to JSON to PostgreSQL. Since the "slug" is unique on
        // the server side, having it be the primary key here should
        // allow SQLite to ignore the record without causing
        // issues with duplicates.
        db.insertWithOnConflict(NAME, null, v, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getThumb_url() {
        return thumb_url;
    }

    public void setThumb_url(String thumb_url) {
        this.thumb_url = thumb_url;
    }

    public String getYapa_url() {
        return yapa_url;
    }

    public void setYapa_url(String yapa_url) {
        this.yapa_url = yapa_url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setYapa_thumb_url(String yapa_thumb_url) {
        this.yapa_thumb_url = yapa_thumb_url;
    }

    public String getYapaThumbUrl() {
        return yapa_thumb_url;
    }

    public void setContentType(String to) {
        yapa_content_type = to;
    }

    public String getContentType() {
        return yapa_content_type;
    }

    public void setContent(String to) {
        content = to;
    }

    public String getContent() {
        return content;
    }

    public void setURI(String to) {
        uri = to;
    }

    public String getURI() {
        return uri;
    }

    public void setTagName(String to) {
        tag_name = to;
    }

    public String getTagName() {
        return tag_name;
    }
}
