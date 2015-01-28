/**
 * Methods and data common to all database access code.
 *
 * Also serves as a singleton to the DatabaseHelper class
 */

package co.tapdatapp.tapandroid.localdata;

import co.tapdatapp.tapandroid.TapApplication;

public class BaseDAO {
  private static DatabaseHelper dbh;

  static {
    dbh = new DatabaseHelper(TapApplication.get());
  }

  protected static DatabaseHelper getDatabaseHelper() {
    if (dbh == null) {
      throw new AssertionError("DatabaseHelper not yet initialized");
    }
    return dbh;
  }


}
