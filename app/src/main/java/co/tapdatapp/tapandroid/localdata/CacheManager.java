/**
 * Manage a cache of binary files. Cleans up when data gets too big.
 */

package co.tapdatapp.tapandroid.localdata;

public class CacheManager extends Thread {

    private static CacheManager thread;

    private boolean enabled = true;

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
        while (enabled) {
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

    /**
     * Request a GC event. Since there is only ever a single thread
     * running GC, multiple requests in rapid succession from multiple
     * threads will simply result in a single GC execution. (Or
     * possibly a few in series, depending on timing) The important
     * thing is that there will never be multiple GC threads running.
     */
    public static void scheduleGC() {
        thread.notifyAll();
    }

    /**
     * Stop the GC thread. This isn't currently used because Android
     * eschews the typical process model and there's no way to know
     * when a process is being shut down. It's good design to have it,
     * but there's just no way to use it.
     *
     * @throws InterruptedException
     */
    public static void shutdown() throws InterruptedException {
        thread.enabled = false;
        thread.notifyAll();
        thread.join();
        thread = null;
    }
}
