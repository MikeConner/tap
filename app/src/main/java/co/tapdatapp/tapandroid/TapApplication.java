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
     * Conventience method to fetch a String resource
     *
     * @param id resource ID
     * @return The string of the specified resource
     */
    public static String string(int id) {
        return app.getString(id);
    }
}
