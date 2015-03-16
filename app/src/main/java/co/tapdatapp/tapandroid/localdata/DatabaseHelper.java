/**
 * The SQLite model is to create a class that descends from
 * SQLiteOpenHelper that essentially encapsulates both the database
 * connection resource, and schema version management.
 */

package co.tapdatapp.tapandroid.localdata;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    /**
     * Name of the database to connect to. This isn't really needed
     * because it's unlikely that a single app will create multiple
     * databases, but it's required.
     */
    private final static String DATABASE_NAME = "tap_app";
    /**
     * Schema version. If any of the table layouts change, this must be
     * incremented and the onCreate() and onUpgrade() methods updated to
     * handle the changes.
     */
    private final static int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Contains all the operations required to create a new database
     * schema.
     *
     * @param sqLiteDatabase connection object
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        new Transaction().onCreate(sqLiteDatabase);
        new AndroidCache().onCreate(sqLiteDatabase);
        new CurrencyDAO().onCreate(sqLiteDatabase);
        new Denomination().onCreate(sqLiteDatabase);
        new Tag().onCreate(sqLiteDatabase);
        new Yapa().onCreate(sqLiteDatabase);
    }

    /**
     * Must have code to handle everything necessary to upgrade a
     * database schema from oldVersion to newVersion. Note that there
     * is no guarantee as to how far apart oldVersion and newVersion are.
     *
     * @param sqLiteDatabase connection object
     * @param oldVersion     The current DATABASE_VERSION
     * @param newVersion     The required DATABASE_VERSION
     */
    @Override
    public void onUpgrade(
        SQLiteDatabase sqLiteDatabase,
        int oldVersion,
        int newVersion
    ) {
        new Transaction().onUpgrade(sqLiteDatabase, oldVersion, newVersion);
        new AndroidCache().onUpgrade(sqLiteDatabase, oldVersion, newVersion);
        new CurrencyDAO().onUpgrade(sqLiteDatabase, oldVersion, newVersion);
        new Denomination().onUpgrade(sqLiteDatabase, oldVersion, newVersion);
        new Tag().onUpgrade(sqLiteDatabase, oldVersion, newVersion);
        new Yapa().onUpgrade(sqLiteDatabase, oldVersion, newVersion);
        // This protects us from bizarre behaviour. As there are no
        // changes so far, this method should never get called, and if it
        // is, something is seriously wrong. As soon as there are actual
        // DB changes to track, this will be removed.
        throw new AssertionError("Unexpected call to database upgrade");
    }
}
