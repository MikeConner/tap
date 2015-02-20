/**
 * Data object wrapping a Transaction
 */

package co.tapdatapp.tapandroid.localdata;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Timestamp;
import java.util.Date;

public class Transaction implements SingleTable, TransactionDAO {
    public final static String NAME = "transactions";
    public final static String SLUG = "slug";
    public final static String TIMESTAMP = "timestamp";
    public final static String THUMB_URL = "thumb_url";
    public final static String YAPA_THUMB_URL = "yapa_thumb_url";
    public final static String YAPA_URL = "yapa_url";
    public final static String DESCRIPTION = "description";
    public final static String AMOUNT = "amount";
    public final static String NICKNAME = "nickname";

    private String slug;
    private Timestamp timestamp;
    private String thumb_url;
    private String yapa_thumb_url;
    private String yapa_url;
    private String description;
    private int amount;
    private String nickname;

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
                NICKNAME + " TEXT NOT NULL " +
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

    @Override
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

    @Override
    public Transaction getByOrder(int i) {
        Transaction rv = new Transaction();
        rv.moveTo(i);
        return rv;
    }

    @Override
    public void moveTo(int location) {
        Cursor c = null;
        try {
            SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
            c = db.query(
                NAME,
                new String[]{
                    SLUG, TIMESTAMP, THUMB_URL, YAPA_URL,
                    DESCRIPTION, AMOUNT, NICKNAME,
                    YAPA_THUMB_URL,
                },
                null, null, null, null,
                TIMESTAMP + " DESC",
                location + ", 1"
            );
            if (c.moveToFirst()) {
                slug = c.getString(0);
                timestamp = new Timestamp(c.getLong(1));
                thumb_url = c.getString(2);
                yapa_url = c.getString(3);
                description = c.getString(4);
                amount = c.getInt(5);
                nickname = c.getString(6);
                yapa_thumb_url = c.getString(7);
            } else {
                throw new Error("No TX record at location " + location);
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    @Override
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
        db.insertOrThrow(NAME, null, v);
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

    public String getYapa_thumb_url() {
        return yapa_thumb_url;
    }

    public void setYapa_thumb_url(String yapa_thumb_url) {
        this.yapa_thumb_url = yapa_thumb_url;
    }
}
