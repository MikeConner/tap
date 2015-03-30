/**
 * Manage a cache of binary files. Cleans up when data gets too big.
 */

package co.tapdatapp.tapandroid.localdata;

import co.tapdatapp.tapandroid.TapApplication;

public class CacheManager extends Thread {

    private static CacheManager thread;

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
     * Limit at which garbage collection will be attempted. This is
     * 0.1% of the available storage space of the device.
     *
     * @return # bytes over which GC should run
     */
    private int getSoftLimit() {
        long rv = TapApplication.getFreeStorage() / 1000;
        if (rv > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        else {
            return (int)rv;
        }
    }

    /**
     * Limit at which *something* is guaranteed to be deleted. This
     * is 50% of the available storage space on the device
     *
     * @return # bytes over which GC _must_ delete something
     */
    private int getHardLimit() {
        long rv = TapApplication.getFreeStorage() / 2;
        if (rv > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        else {
            return (int)rv;
        }
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

    /**
     * The purpose of a dedicated CacheManager thread is to loop and
     * do garbage collection as long as the thread is enabled.
     *
     * Garbage collection will run whenever explicitly requested, or
     * every 30 seconds if no requests are made. Request are made
     * simply by notify()ing the thread.
     */
    @Override
    public void run() {
        while (true) {
            gc();
            synchronized (this) {
                try {
                    wait(30, 0);
                }
                catch (InterruptedException ie) {
                    // Just proceed through to the next iteration
                }
            }
        }
    }

    /**
     * Singleton instantiation. Start a dedicated thread for cache
     * management to keep things sane when lots of threads are trying
     * to use the cache.
     *
     * @param cache A cache object to actually store/retrieve the data
     */
    public static void startUp(Cache cache) {
        if (thread != null) {
            throw new AssertionError("CacheManager already started");
        }
        thread = new CacheManager(cache);
        thread.start();
    }
}
