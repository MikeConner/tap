package co.tapdatapp.tapandroid.localdata;

import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import com.amazonaws.services.s3.internal.Mimetypes;

import java.util.NoSuchElementException;
import java.util.Random;

public class AndroidCacheTest extends AndroidTestCase {

    private Random r = new Random();

    /**
     * Broad strokes: Test put/get/remove in one test
     */
    public void testGeneralProcess() {
        final String NAME = "GENERAL_PROCESS_TEST";
        Cache c = new AndroidCache();
        byte[] data = randomData();
        c.put(NAME, Mimetypes.MIMETYPE_OCTET_STREAM, data);
        byte[] out = c.get(NAME);
        c.remove(NAME);
        // Regular assertEquals doesn't work on arrays
        MoreAsserts.assertEquals("Failed retrieval", data, out);
        try {
            c.get(NAME);
            fail("Should throw NoSuchElementException");
        }
        catch (NoSuchElementException nsee) {
            // This is correct behavior
        }
    }

    /**
     * Test that mime-type is properly stored/retrieved
     */
    public void testMimeType() {
        final String NAME = "MIME_TYPE_TEST";
        Cache c = new AndroidCache();
        byte[] data = randomData();
        c.put(NAME, Mimetypes.MIMETYPE_OCTET_STREAM, data);
        String rv = c.getType(NAME);
        c.remove(NAME);
        assertEquals("mime-type mismatch", Mimetypes.MIMETYPE_OCTET_STREAM, rv);
        try {
            c.get(NAME);
            fail("Should throw NoSuchElementException");
        }
        catch (NoSuchElementException nsee) {
            // This is correct behavior
        }
    }

    public void testOverwrite() {
        final String NAME = "OVERWRITE_TEST";
        Cache c = new AndroidCache();
        byte[] data = randomData();
        c.put(NAME, Mimetypes.MIMETYPE_OCTET_STREAM, randomData());
        try {
            c.put(NAME, Mimetypes.MIMETYPE_GZIP, data);
            fail("Cache object overwritten");
        }
        catch (Exception e) {
            // Expected behavior, it's an error to save the same
            // object 2x
        }
        finally {
            c.remove(NAME);
        }
    }

    public void testNoSuchObject() {
        final String NAME = "NO_OBJECT_TEST";
        Cache c = new AndroidCache();
        try {
            c.get(NAME);
            fail("Exception should be thrown for no object found");
        }
        catch (NoSuchElementException nsee) {
            // Correct behavior
        }
    }

    /**
     * Tests that removing a nonexistent object doesn't error
     */
    public void testRemovalSilent() {
        final String NAME = "SILENT_REMOVAL_TEST";
        Cache c = new AndroidCache();
        c.remove(NAME);
    }

    public void testGetTotalSize0() {
        // Cache should be empty, size should be 0
        Cache c = new AndroidCache();
        int result = c.getTotalSize();
        assertEquals("Size should be 0", 0, result);
    }

    public void testGetTotalSize1() {
        final String NAME = "TOTAL_SIZE_1";
        byte[] data = randomData();
        int expected = data.length;
        Cache c = new AndroidCache();
        c.put(NAME, "", data);
        int result = c.getTotalSize();
        c.remove(NAME);
        assertEquals("Single element size failure", expected, result);
    }

    public void testGetTotalSize2() {
        final String NAME0 = "TOTAL_SIZE_2_0";
        final String NAME1 = "TOTAL_SIZE_2_1";
        byte[] data0 = randomData();
        byte[] data1 = randomData();
        int expected = data0.length + data1.length;
        Cache c = new AndroidCache();
        c.put(NAME0, "", data0);
        c.put(NAME1, "", data1);
        int result = c.getTotalSize();
        c.remove(NAME0);
        c.remove(NAME1);
        assertEquals("Multi element size failure", expected, result);
    }

    /**
     * Generate random binary data for testing
     * @return random length array of random bytes
     */
    private byte[] randomData() {
        int length = r.nextInt(1024);
        byte[] rv = new byte[length];
        for (int i = 0; i < length; i++) {
            rv[i] = (byte)r.nextInt(255);
        }
        return rv;
    }

}
