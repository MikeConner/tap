/**
 * Provides a single point of control for global resources. By design,
 * Android discourages global resources, but practicality means that
 * there have to be a few.
 */

package co.tapdatapp.tapandroid;

import android.app.Application;

import co.tapdatapp.tapandroid.localdata.AndroidCache;
import co.tapdatapp.tapandroid.localdata.CacheManager;

public class TapApplication extends Application {
    /**
     * Store the global Application object so that it can be fetched
     * by objects that aren't Activities. This prevents having to pass
     * the Context to every method all the time. This makes the code
     * a lot cleaner in many places, and also makes more sense where
     * the Application Context (and not an Activity Context) is what
     * is needed anyway (such as when working with SQLite).
     */
    private static TapApplication app;

    /**
     * On startup, stash the Application object, and start the
     * CacheManager thread.
     */
    @Override
    public void onCreate() {
        app = this;
        // Would be nice to shut this down cleanly as well, but
        // Android provides no way to know when the application is
        // being stopped.
        CacheManager.startUp(new AndroidCache());
    }

    /**
     * Return the Application object, so code doesn't have to chase
     * the Context around.
     *
     * @return Application object
     */
    public static TapApplication get() {
        return app;
    }

    /**
     * Convenience method to fetch a String resource
     *
     * @param id resource ID
     * @return The string of the specified resource
     */
    public static String string(int id) {
        return app.getString(id);
    }

    /**
     * Convenience method to get an integer from a string resource
     *
     * @param id resource ID
     * @return String representation of the requested resource
     */
    public static int integer(int id) {
        return Integer.parseInt(app.getString(id));
    }

    /**
     * Convenience method to get a character array from a string resource
     *
     * @param id resource ID
     * @return char[] of the resource string
     */
    public static char[] charArray(int id) {
        return app.getString(id).toCharArray();
    }

    /**
     * Call this method when a completely unexpected error (such as an
     * OOM or NPE) happens.
     *
     * DO NOT call this method with typical errors such as a network
     * failure or NFC unavailable problem (which should be handled
     * by telling the user to find a network or telling them to
     * turn on their NFC). This method is purely for catching and
     * handling things that we would never expect to happen.
     *
     * This method will eventually perform 3 actions
     * 1) Handle known exceptions in a sane way when possible
     * 2) Present a friendly, generic error message to the user when necessary
     * 3) Push error data to the server when it can't be managed locally
     *
     * @param t Exception that caused the problem
     */
    // @TODO bounce this up to the error reporting service on the server
    // @TODO show a generic "unknown error" message to the user
    public static void unknownFailure(Throwable t) {
        throw new AssertionError(t);
    }
}
