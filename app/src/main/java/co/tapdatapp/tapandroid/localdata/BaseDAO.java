/**
 * Methods and data common to all database access code.
 *
 * Also serves as a singleton to the DatabaseHelper class
 */

package co.tapdatapp.tapandroid.localdata;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import co.tapdatapp.tapandroid.TapApplication;

public class BaseDAO {
    private static DatabaseHelper dbh;

    static {
        dbh = new DatabaseHelper(TapApplication.get());
    }

    /**
     * Returns singleton of the DatabaseHelper class. This protects
     * from deadlocks when multiple threads try to talk to the database
     * at once, since a single SQLOpenHelper class is thread-safe and
     * will serialize queries.
     *
     * @return DatabaseHelper object
     */
    protected static DatabaseHelper getDatabaseHelper() {
        if (dbh == null) {
            throw new AssertionError("DatabaseHelper not yet initialized");
        }
        return dbh;
    }

    /**
     * Simplify the call to update a single column in a table
     *
     * @param table target table
     * @param column column to update
     * @param newValue new value for column
     * @param where where clause
     * @param whereArgs arguments to replace ? in where clause
     * @return Number of rows updated
     */
    protected int update(String table,
                          String column,
                          long newValue,
                          String where,
                          String[] whereArgs
    ) {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        ContentValues values = new ContentValues();
        values.put(column, newValue);
        return db.update(table, values, where, whereArgs);
    }

}
