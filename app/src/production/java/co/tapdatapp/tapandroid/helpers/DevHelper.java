/**
 * This class contains methods for making development and testing
 * easier. The version in the main flavor contains no code, just
 * empty methods, thus in production builds it adds no functionality.
 * The version in the dev flavor contains code that adds various
 * abilities to the app.
 *
 */

package co.tapdatapp.tapandroid.helpers;

public class DevHelper {

    public static boolean isEnabled(int feature) {
        return false;
    }
}
