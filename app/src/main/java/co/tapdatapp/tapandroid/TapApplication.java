package co.tapdatapp.tapandroid;

import android.app.Application;

public class TapApplication extends Application {
    private static TapApplication app;

    @Override
    public void onCreate() {
        app = this;
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
