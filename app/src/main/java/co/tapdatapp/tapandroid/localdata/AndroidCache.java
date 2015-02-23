/**
 * Keep a local cache of files (images and other media)
 *
 * Because the class uses byte[] as intermediary storage, this can't
 * handle files larger than 2G. Which may become a problem if the
 * system starts shipping around media such as videos. The actual max
 * size might actually be much smaller than 2G, depending on Android's
 * memory limitations.
 *
 * There are a lot of redundant queries, however, the fact that there
 * are no instance variables means that this implementation is thread
 * safe. The more I think about it, the more I think that safety is
 * more important than any performance boost that would come from
 * pre-loading cache objects into memory.
 *
 * @TODO handle larger file sizes? (maybe)
 *
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

public class AndroidCache extends BaseDAO implements SingleTable, Cache {

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

    @Override
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

    @Override
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

    @Override
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
            // Since Android is allowed to remove files from the cache
            // directory at any time, this is pretty likely to happen.
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

    @Override
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void remove(String name) {
        // By deleting the file first, a crash or interruption results
        // in an orphaned DB record, which the code is designed to
        // handle anyway, and not an orphaned file, which would leak
        // storage space.
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

    @Override
    public int getTotalSize() {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery(
                "SELECT sum(" +  SIZE + ") FROM " + TABLE,
                null
            );
            if (c.getCount() != 1) {
                throw new AssertionError(
                    c.getCount() + " records totaling size"
                );
            }
            c.moveToFirst();
            return c.getInt(0);
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }

    @Override
    public long getLastAccessed(String name) {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery(
                "SELECT " + LAST_ACCESS + " FROM " + TABLE + " WHERE " + NAME + " = ?",
                new String[]{name}
            );
            if (c.getCount() != 1) {
                throw new NoSuchElementException(
                    c.getCount() + " records found for " + name
                );
            }
            c.moveToFirst();
            return c.getLong(0);
        }
        finally {
            if (c != null) {
                c.close();
            }
        }
    }

    @Override
    public String getOldest() {
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        Cursor c = null;
        try {
            c = db.query(
                TABLE,
                new String[] { NAME },
                null, null, // Empty WHERE clause
                null, null, // Empty GROUP BY/HAVING
                LAST_ACCESS + " ASC",
                "1" // LIMIT 1
            );
            if (c.getCount() != 1) {
                throw new NoSuchElementException(
                    c.getCount() + " records found for oldest"
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
     * Update the last modified column in the cache table with the
     * current time
     *
     * @param name identifier of the file to be updated
     */
    private void updateLastModified(String name) {
        int rows = update(
            TABLE,
            LAST_ACCESS,
            System.currentTimeMillis() / 1000,
            NAME + " = ?",
            new String[] {name}
        );
        if (rows != 1) {
            throw new NoSuchElementException(
                rows + " objects modified setting last update time"
            );
        }
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
        if (name == null || name.isEmpty()) {
            throw new NullPointerException("Null or empty item name");
        }
        SQLiteDatabase db = BaseDAO.getDatabaseHelper().getReadableDatabase();
        Cursor c = null;
        try {
            String qString = "SELECT " + FILENAME + " FROM " + TABLE + " WHERE " + NAME + " = ?";
            c = db.rawQuery(qString, new String[]{name});
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

}
