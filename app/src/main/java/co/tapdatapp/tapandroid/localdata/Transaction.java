/**
 * Data object wrapping a Transaction
 */

package co.tapdatapp.tapandroid.localdata;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.Timestamp;

public class Transaction implements SingleTable, TransactionDAO {
  public final static String NAME = "transactions";
  public final static String SLUG = "slug";
  public final static String TIMESTAMP = "timestamp";
  public final static String THUMB_URL = "thumb_url";
  public final static String THUMB_LOCAL = "thumb_local";
  public final static String YAPA_URL = "yapa_url";
  public final static String YAPA_LOCAL = "yapa_local";
  public final static String DESCRIPTION = "description";
  public final static String AMOUNT = "amount";

  private String slug;
  private Timestamp timestamp;
  private String thumb_url;
  private String thumb_local;
  private String yapa_url;
  private String yapa_local;
  private String description;
  private int amount;

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(
      "CREATE TABLE " + NAME + " ( " +
      SLUG + " TEXT PRIMARY KEY, " +
      TIMESTAMP + " TEXT NOT NULL, " +
      THUMB_URL + " TEXT NOT NULL, " +
      THUMB_LOCAL + " TEXT, " +
      YAPA_URL + " TEXT NOT NULL, " +
      YAPA_LOCAL + " TEXT, " +
      DESCRIPTION + " TEXT NOT NULL, " +
      AMOUNT + " DECIMAL NOT NULL " +
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
      }
      else {
        throw new Error("Unexplained failure to count transactions");
      }
    }
    finally {
      if (c != null) {
        c.close();
      }
    }
  }

  public Transaction getByOrder(int i) {
    Transaction rv = new Transaction();
    rv.moveTo(i);
    return rv;
  }

  public void moveTo(int location) {
    Cursor c = null;
    try {
      SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
      c = db.query(
        NAME,
        new String[]{SLUG, TIMESTAMP, THUMB_URL, THUMB_LOCAL, YAPA_URL, YAPA_LOCAL, DESCRIPTION, AMOUNT},
        null, null, null, null, TIMESTAMP + " DESC", location + ", 1"
      );
      if (c.moveToFirst()) {
        slug = c.getString(0);
        timestamp = Timestamp.valueOf(c.getString(1));
        thumb_url = c.getString(2);
        thumb_local = c.getString(3);
        yapa_url = c.getString(4);
        yapa_local = c.getString(5);
        description = c.getString(6);
        amount = c.getInt(7);
      }
      else {
        throw new Error("No TX record at location " + location);
      }
    }
    finally {
      if (c != null) {
        c.close();
      }
    }
  }

  public void create() {
    SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
    ContentValues v = new ContentValues();
    v.put(SLUG, slug);
    v.put(TIMESTAMP, timestamp.toString());
    v.put(THUMB_URL, thumb_url);
    v.put(THUMB_LOCAL, thumb_local);
    v.put(YAPA_URL, yapa_url);
    v.put(YAPA_LOCAL, yapa_local);
    v.put(DESCRIPTION, description);
    v.put(AMOUNT, amount);
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

  public String getThumb_local() {
    return thumb_local;
  }

  public void setThumb_local(String thumb_local) {
    this.thumb_local = thumb_local;
  }

  public String getYapa_url() {
    return yapa_url;
  }

  public void setYapa_url(String yapa_url) {
    this.yapa_url = yapa_url;
  }

  public String getYapa_local() {
    return yapa_local;
  }

  public void setYapa_local(String yapa_local) {
    this.yapa_local = yapa_local;
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
}
