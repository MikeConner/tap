package co.tapdatapp.tapandroid.localdata;

import android.test.AndroidTestCase;

import org.junit.Before;

public class CacheManagerTest extends AndroidTestCase {

    private MockCache cache;
    private CacheManager manager;

    @Before
    public void setUp() {
        cache = new MockCache();
        manager = new CacheManager(cache);
    }

    /**
     * Test that a GC does nothing when everything is under limits.
     */
    public void testGCUnderLimits() {
        cache.totalSize = 5;
        manager.gc();
        assertEquals("Should not have removed anything", null, cache.removedName);
    }

    /**
     * Test that GC does nothing when size is between limits and there
     * are no objects older than the expire age
     */
    public void testGCBetweenLimitsNoDelete() {
        cache.totalSize = 10 * 1024;
        cache.lastAccessed = (System.currentTimeMillis() / 1000) - 60;
        manager.gc();
        assertEquals("Should not have removed anything", null, cache.removedName);
    }

    /**
     * Test that GC deletes the oldest when between limits and the
     * oldest is older than the expire age
     */
    public void testGCBetweenLimitsDelete() {
        final String NAME = "TEST_OLDEST_TO_DELETE";
        cache.totalSize = 1024 * 1024;
        cache.lastAccessed = (System.currentTimeMillis() / 1000) - (48 * 60 * 60);
        cache.oldestName = NAME;
        manager.gc();
        assertEquals("Failed to remove", NAME, cache.removedName);
    }

    /**
     * Tests that the GC deletes the oldest item when over the hard
     * limit, regardless of its age
     */
    public void testGCOverHardLimit() {
        final String NAME = "TEST_OLDEST_TO_DELETE";
        cache.totalSize = 10 * 1024 * 1024;
        cache.lastAccessed = (System.currentTimeMillis() / 1000) - 60;
        cache.oldestName = NAME;
        manager.gc();
        assertEquals("Should have removed this", NAME, cache.removedName);
    }
}
