/**
 * Cache interface for put/get binary data
 */

package co.tapdatapp.tapandroid.localdata;

public interface Cache {
    /**
     * Cache a copy of the provided file.
     *
     * @param name unique name for the file
     * @param mediaType mime type
     * @param data the data of the file
     */
    void put(String name, String mediaType, byte[] data);

    /**
     * Get the mime type for a file
     *
     * @param name name of the file
     * @return mime type
     */
    String getType(String name);

    /**
     * Get the data of a file
     *
     * @param name file identifier
     * @return file data
     */
    byte[] get(String name);

    /**
     * Remove a file from the cache (both the DB record and data)
     *
     * Does not throw exceptions. Removes whatever it can find, and
     * silently ignores any parts that are missing.
     *
     * @param name identifier of the file
     */
    void remove(String name);

    /**
     * @return # of bytes used by all objects in the cache
     */
    int getTotalSize();

    /**
     * @return the identifier of the least recently used object
     */
    String getOldest();

    /**
     * @param name name of a cache object to query
     * @return last accessed time for specified object, in epoch time
     */
    long getLastAccessed(String name);
}
