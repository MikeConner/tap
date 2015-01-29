/**
 * Keep a local cache of files (images and other media)
 *
 * Because the class uses byte[] as intermediary storage, this can't
 * handle files larger than 2G. Which may become a problem if the
 * system starts shipping around media such as videos. The actual max
 * size might actually be much smaller than 2G, depending on Android's
 * memory limitations.
 *
 * @TODO handle larger file sizes? (maybe)
 *
 * @TODO there are a lot of redundant queries
 */

package co.tapdatapp.tapandroid.localdata;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.UUID;

import co.tapdatapp.tapandroid.TapApplication;

public class Cache extends BaseDAO implements SingleTable {

    private final static String TABLE = "cache";
    /**
     * This is the name provided by other code, must be unique
     */
    private final static String NAME = "name";
    /**
     * Filename of the actual file where data is stored, generated
     * by this class.
     */
    private final static String FILENAME = "filename";
    /**
     * Size of the file in bytes
     */
    private final static String SIZE = "size";
    /**
     * mime-type
     */
    private final static String TYPE = "type";
    /**
     * Last access in epoch time
     */
    private final static String LAST_ACCESS = "last_access";

    /**
     * Cache a copy of the provided file.
     *
     * @param name unique name for the file
     * @param mediaType mime type
     * @param data the data of the file
     */
    @SuppressWarnings("ThrowFromFinallyBlock")
    public void put(String name, String mediaType, byte[] data) {
        String id = UUID.randomUUID().toString();
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put(NAME, name);
        v.put(FILENAME, id);
        v.put(SIZE, data.length);
        v.put(TYPE, mediaType);
        v.put(LAST_ACCESS, System.currentTimeMillis() / 1000);
        db.insertOrThrow(TABLE, null, v);
        String filename = getFullFilespec(id);
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filename, false);
            try {
                os.write(data);
            }
            catch (IOException ioe) {
                // Assumption is that we're out of storage space.
                os.close();
                remove(name);
                throw new RuntimeException(ioe);
            }
        } catch (IOException e) {
            remove(name);
            throw new RuntimeException(e);
        }
        finally {
            if (os != null) {
                try {
                    os.close();
                }
                catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            }
        }
    }

    /**
     * Get the mime type for a file
     *
     * @param name name of the file
     * @return mime type
     */
    public String getType(String name) {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery(
                "SELECT " + TYPE + " FROM " + TABLE + " WHERE " + NAME + " = ?",
                new String[]{name}
            );
            if (c.getCount() != 1) {
                throw new NoSuchElementException(
                    c.getCount() + " records found for " + name
                );
            }
            c.moveToFirst();
            return c.getString(0);
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Get the data of a file
     *
     * @param name file identifier
     * @return file data
     */
    @SuppressWarnings("ThrowFromFinallyBlock")
    public byte[] get(String name) {
        String filename = getFullFilespec(getFilename(name));
        File f = new File(filename);
        long size = f.length();
        if (size > Integer.MAX_VALUE) {
            throw new AssertionError("File " + name + " too large");
        }
        byte[] data = new byte[(int)size];
        FileInputStream is = null;
        try {
            is = new FileInputStream(filename);
            int read = is.read(data);
            if (read != size) {
                throw new AssertionError(
                    "Read " + read + " of file " + name + " size " + size
                );
            }
        }
        catch (FileNotFoundException fnfe) {
            remove(name);
            throw new NoSuchElementException(name);
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            }
        }
        updateLastModified(name);
        return data;
    }

    /**
     * Remove a file from the cache (both the DB record and data)
     *
     * Does not throw exceptions. Removes whatever it can find, and
     * silently ignores any parts that are missing.
     *
     * @param name identifier of the file
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void remove(String name) {
        try {
            File f = new File(getFullFilespec(getFilename(name)));
            f.delete();
        }
        catch (NoSuchElementException nsee) {
            // This means the DB doesn't have the record, so
            // return without propagating any error
            return;
        }
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getWritableDatabase();
        db.delete(TABLE, NAME + " = ?", new String[] {name});
    }

    /**
     * Update the last modified column in the cache table with the
     * current time
     *
     * @param name identifier of the file to be updated
     */
    private void updateLastModified(String name) {
        update(
            TABLE,
            LAST_ACCESS,
            Long.toString(System.currentTimeMillis() / 1000),
            NAME + " = ?",
            new String[] {name}
        );
    }

    /**
     * Return a full path + filename from filename alone. Automatically
     * finds the correct cache directory and prepends it.
     *
     * @param filename filename part
     * @return full path + filename
     */
    private String getFullFilespec(String filename) {
        return TapApplication.get().getCacheDir().toString() + "/" + filename;
    }

    /**
     * Get the filename from the provided identifier
     *
     * @param name identifier of the file
     * @return The filename generated by the class
     */
    private String getFilename(String name) {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery(
                "SELECT " + FILENAME + " FROM " + TABLE + " WHERE " + NAME + " = ?",
                new String[]{name}
            );
            if (c.getCount() != 1) {
                throw new NoSuchElementException(
                    c.getCount() + " records found for " + name
                );
            }
            c.moveToFirst();
            return c.getString(0);
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }

    /**
     * Data table holds metadata for the file. Only called from
     * DatabaseHelper
     *
     * @param db Db connection
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
            "CREATE TABLE " + TABLE + " ( " +
                NAME + " TEXT PRIMARY KEY, " +
                FILENAME + " TEXT UNIQUE NOT NULL, " +
                SIZE + " INT NOT NULL, " +
                TYPE + " TEXT NOT NULL, " +
                LAST_ACCESS + " BIGINT NOT NULL " +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // No upgrade actions to do
    }

    /**
     * Limit at which garbage collection will be attempted.
     *
     * @return # bytes over which GC should run
     */
    // @TODO this should be calculated from the device's storage size
    private int getSoftLimit() {
        // 500K
        return 500 * 1024;
    }

    /**
     * Limit at which *something* is guarnteeed to be deleted
     *
     * @return # bytes over which GC _must_ delete something
     */
    // @TODO this should be calculated from the device's storage size
    private int getHardLimit() {
        // 5M
        return 5 * 1024 * 1024;
    }

    /**
     * When the soft limit is reached, files older than the specified
     * age are candidates for deletion.  This value is actually a
     * candidate for a constant, as 24 hours is probably a good all-
     * around value (since the hard limit will take care of cleanup if
     * it's not good enough)
     *
     * @return time in seconds since last access to expire an item
     */
    private int getExpire() {
        return 24 * 60 * 60;
    }
}
