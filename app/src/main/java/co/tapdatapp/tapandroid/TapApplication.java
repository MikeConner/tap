/**
 * Provides a single point of control for global resources. By design,
 * Android discourages global resources, but practicality means that
 * there have to be a few.
 */

package co.tapdatapp.tapandroid;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import co.tapdatapp.tapandroid.helpers.DevHelper;
import co.tapdatapp.tapandroid.helpers.UserFriendlyError;
import co.tapdatapp.tapandroid.localdata.AndroidCache;
import co.tapdatapp.tapandroid.localdata.CacheManager;
import co.tapdatapp.tapandroid.remotedata.NoNetworkError;

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
        ActivityManager am = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        Log.d("MEMORY", "Memory class = " + am.getMemoryClass());
        Log.d("MEMORY", "Large memory class = " + am.getLargeMemoryClass());
        Log.d("STORAGE", "Available storage = " + getStorage());
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
     * @return Size in bytes of the device's primary storage
     */
    // Note that the non-deprecated methods are too new to be used:
    // they aren't supported by older versions of Android
    @SuppressWarnings("deprecation")
    public static long getStorage() {
        StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
        return (long)stat.getBlockCount() * (long)stat.getBlockSize();
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
     * For typical failure handling cases. Does the most logical
     * thing possible based on the failure class.
     */
    public static void handleFailures(Activity activity, Throwable t) {
        try{
            throw t;
        }
        catch (NoNetworkError nne) {
            showNoConnectionDialog(activity);
        }
        catch (OutOfMemoryError oome) {
            errorToUser(string(R.string.out_of_memory));
        }
        catch (UserFriendlyError ufe) {
            errorToUser(ufe);
        }
        catch (Throwable catchall) {
            unknownFailure(t);
        }
    }

    /**
     * Show a dialog telling the user that there is no network access
     * and give them the option to open the network settings app.
     */
    public static void showNoConnectionDialog(final Activity a) {
        AlertDialog.Builder builder = new AlertDialog.Builder(a);
        builder.setCancelable(true);
        builder.setMessage(R.string.no_network);
        builder.setTitle(R.string.no_network_title);
        builder.setPositiveButton(
            R.string.button_network_settings,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
                }
            }
        );
        builder.setNegativeButton(
            R.string.button_return_to_app,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }
        );
        builder.setOnCancelListener(
            new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                }
            }
        );
        builder.show();
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
     * @param t Exception that caused the problem
     */
    private static void unknownFailure(Throwable t) {
        if (DevHelper.isEnabled(R.string.CRASH_ON_FAILURE)) {
            throw new AssertionError(t);
        }
        else {
            errorToUser(string(R.string.unknown_error));
            errorToServer(t);
        }
    }

    public static void errorToUser(UserFriendlyError e) {
        if (DevHelper.isEnabled(R.string.CRASH_ON_FAILURE)) {
            Log.e("GENERAL", "errorToUser()", e);
        }
        if (e.hasUserError()) {
            errorToUser(e.getUserError());
        }
        else {
            unknownFailure(e);
        }
    }

    /**
     * Call this method any time you need to send an error message to
     * a user. This implementation uses Android's Toast, but by
     * keeping it centralized, we can make the presentation more
     * elaborate if need be.
     *
     * @param message The message to display
     */
    public static void errorToUser(String message) {
        Toast t = Toast.makeText(app, message, Toast.LENGTH_LONG);
        t.show();
    }

    /**
     * Send an exception to the server error reporting endpoint so
     * we can diagnose it later.
     *
     * @param t The exception that cause the error
     */
    // @TODO add the guts to make this work
    public static void errorToServer(Throwable t) {

    }
}
