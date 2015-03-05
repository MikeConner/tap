/**
 * Methods and data common to all database access code.
 *
 * Also serves as a singleton to the DatabaseHelper class
 */

package co.tapdatapp.tapandroid.localdata;

import android.content.ContentValues;
import android.database.Cursor;
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

    /**
     * Count the number of records in the specified table
     *
     * @param tableName Table to count
     * @return number of records in the table
     */
    protected int getRecordCount(String tableName) {
        Cursor c = null;
        try {
            SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
            c = db.rawQuery("SELECT count(*) FROM " + tableName, null);
            if (c.moveToFirst()) {
                return c.getInt(0);
            } else {
                throw new Error("Unexplained failure to count records");
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Simplify the call to fetch a single string value from a single
     * row in a table.
     */
    protected String getString(String table,
                               String column,
                               String where,
                               String[] whereArgs
    ) {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query(
                table,
                new String[]{column},
                where,
                whereArgs,
                null, null, null, null
            );
            if (c.moveToFirst()) {
                return c.getString(0);
            } else {
                throw new RuntimeException("Rows returned = " + c.getCount());
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
    }

    protected Cursor getCursor(String table,
                               String[] columns,
                               String where,
                               String[] whereArgs
    ) {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        return db.query(
            table,
            columns,
            where,
            whereArgs,
            null, null, null, null
        );
    }

}
