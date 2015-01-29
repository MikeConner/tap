package co.tapdatapp.tapandroid.localdata;

import android.test.AndroidTestCase;
import android.test.MoreAsserts;

import com.amazonaws.services.s3.internal.Mimetypes;

import java.util.NoSuchElementException;
import java.util.Random;

public class CacheTest extends AndroidTestCase {

    private Random r = new Random();

    /**
     * Broad strokes: Test put/get/remove in one test
     */
    public void testGeneralProcess() {
        final String NAME = "GENERAL_PROCESS_TEST";
        Cache c = new Cache();
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
        Cache c = new Cache();
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
        Cache c = new Cache();
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
        Cache c = new Cache();
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
        Cache c = new Cache();
        c.remove(NAME);
    }

    private byte[] randomData() {
        int length = r.nextInt(1024);
        byte[] rv = new byte[length];
        for (int i = 0; i < length; i++) {
            rv[i] = (byte)r.nextInt(255);
        }
        return rv;
    }

}
