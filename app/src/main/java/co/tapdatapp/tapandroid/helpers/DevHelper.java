/**
 * This class contains methods for making development and testing
 * easier.
 */

package co.tapdatapp.tapandroid.helpers;

import co.tapdatapp.tapandroid.TapApplication;

public class DevHelper {

    /**
     * @param feature An ID matching a value in R.string
     * @return True if the feature is enabled
     */
    public static boolean isEnabled(int feature) {
        String v = TapApplication.string(feature);
        return v.equalsIgnoreCase("true");
    }
}
