/**
 * Misc code to make dealing with date/time easier
 */

package co.tapdatapp.tapandroid.helpers;

public class DateTime {

    /**
     * @return the current time in seconds from Jan 1, 1970 UTC
     */
    public static long currentEpochTime() {
        return System.currentTimeMillis() / 1000;
    }
}
