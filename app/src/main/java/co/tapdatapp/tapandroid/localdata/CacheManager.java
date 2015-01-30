/**
 * Manage a cache of binary files. Cleans up when data gets too big.
 */

package co.tapdatapp.tapandroid.localdata;

public class CacheManager {

    private Cache cache;

    public CacheManager(Cache c) {
        cache = c;
    }

    /**
     * Garbage collect items if necessary
     *
     * This is not a complete sweep, it may remove 1 or 0 objects.
     * It is intended to be called frequently, reducing the cache
     * size a little each time the conditions are met.
     *
     * Functioning: If the cache size exceeds the hard limit, then the
     * oldest item in the cache is removed. If the size is between
     * the hard and soft limit, then the oldest item in the cache is
     * removed *if* it is older than the expire age. Otherwise, nothing
     * is done.
     */
    public void gc() {
        int totalSize = cache.getTotalSize();
        if (totalSize > getHardLimit()) {
            cache.remove(cache.getOldest());
        }
        else if (totalSize > getSoftLimit()) {
            String name = cache.getOldest();
            long accessed = cache.getLastAccessed(name);
            long now = System.currentTimeMillis() / 1000;
            if ((accessed + getExpire()) < now) {
                cache.remove(name);
            }
        }
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
     * Limit at which *something* is guaranteed to be deleted
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
