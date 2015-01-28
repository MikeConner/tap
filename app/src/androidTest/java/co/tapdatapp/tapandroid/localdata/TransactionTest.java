package co.tapdatapp.tapandroid.localdata;

import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import org.junit.After;

import java.sql.Timestamp;
import java.util.UUID;

public class TransactionTest extends AndroidTestCase {

  /**
   * Remove any records from the table at the end of each test
   */
  @After
  public void tearDown() {
    SQLiteDatabase db = BaseDAO.getDatabaseHelper().getWritableDatabase();
    db.delete(Transaction.NAME, null, null);
  }

  public void testGetRecordCount() {
    Transaction t = new Transaction();
    int rowCount = t.getRecordCount();
    assertEquals("There should be no rows", 0, rowCount);
  }

  public void testBasicSave() {
    Transaction t = new Transaction();
    createRandomTransaction(t);
    t.create();
    assertEquals("There should be a single record", 1, t.getRecordCount());
    t = new Transaction();
    createRandomTransaction(t);
    t.create();
    assertEquals("There should be 2 transactions", 2, t.getRecordCount());
  }

  public void testRetrieve() {
    Transaction t = new Transaction();
    createRandomTransaction(t);
    t.create();
    Transaction t1 = new Transaction();
    t1.moveTo(0);
    assertEquals("Retrieval error", t.getDescription(), t1.getDescription());
    assertEquals("Retrieval error", t.getAmount(), t1.getAmount());
    assertEquals("Retrieval error", t.getSlug(), t1.getSlug());
    assertEquals("Retrieval error", t.getThumb_url(), t1.getThumb_url());
    assertEquals("Retrieval error", t.getYapa_url(), t1.getYapa_url());
    assertEquals("Retrieval error", t.getTimestamp(), t1.getTimestamp());
    assertEquals("Retrieval error", null, t1.getYapa_local());
  }

  private void createRandomTransaction(Transaction t) {
    t.setAmount(5);
    t.setDescription("Generated Test transaction");
    t.setSlug(UUID.randomUUID().toString());
    t.setThumb_url("http://www.example.com");
    t.setYapa_url("http://www.example.com");
    t.setTimestamp(new Timestamp(50));
  }

}
