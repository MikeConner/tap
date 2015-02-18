/**
 * Wrapper to simplify parsing and formatting of ISO8601 timestamps
 */

package co.tapdatapp.tapandroid.helpers;

import java.text.SimpleDateFormat;

public class ISO8601Format extends SimpleDateFormat {

    public ISO8601Format() {
        super("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    }

}
