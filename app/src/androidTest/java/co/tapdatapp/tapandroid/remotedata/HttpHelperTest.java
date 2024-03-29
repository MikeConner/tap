/**
 * Because the various classes for fetching webservice data are tightly
 * coupled, this single test class tests many of them together.
 *
 * @TODO need a way to automatically create an auth token prior
 */

package co.tapdatapp.tapandroid.remotedata;

import android.os.Bundle;
import android.test.AndroidTestCase;

import org.junit.Before;

import java.io.IOException;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.user.Account;

public class HttpHelperTest extends AndroidTestCase {

    private HttpHelper helper;

    /**
     * Note that at this time, the authentication is wonky, and
     * the tests are only good if you first run the app on the testing
     * device to create an account token there.
     */
    @Before
    public void setUp() {
        Account a = new Account();
        if (!a.created()) {
            fail("Empty auth token");
        }
        helper = new HttpHelper();
    }

    /**
     * Ensure a non-existent URL produces the desired result
     * (Knowing my luck, someone will create this URL)
     */
    public void test404Response() throws IOException {
        WebResponse r = helper.HttpGet(
            "http://www.tapdatapp.co/thisWillNeverExist.never",
            new Bundle()
        );
        assertFalse("Response should have been an error", r.isOK());
    }

    public void testMediaType() throws IOException {
        WebResponse r = helper.HttpGet(
            "http://www.tapdatapp.co",
            new Bundle()
        );
        assertEquals("Wrong media type", "text/html", r.getMediaType());
    }

    public void testAppendAuthTokenIfExists0() {
        StringBuilder value = new StringBuilder("");
        helper.appendAuthTokenIfExists(value, true);
        assertTrue("Should start with &", value.toString().startsWith("&"));
    }

    public void testAppendAuthTokenIfExists1() {
        StringBuilder value = new StringBuilder("");
        helper.appendAuthTokenIfExists(value, false);
        assertTrue("Should start with ?", value.toString().startsWith("?"));
    }
}
