/**
 * Common functionality for ListAdapters
 */

package co.tapdatapp.tapandroid.localdata;

import android.app.Activity;
import android.database.DataSetObserver;
import android.widget.ListAdapter;

public abstract class BaseAdapter implements ListAdapter {

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        // The dataset is static. When it changes a new adapter is
        // created, so DSOs don't need to be tracked.  Override if
        // there is need of a DSO
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        // The dataset is static. When it changes a new adapter is
        // created, so DSOs don't need to be tracked. Override if
        // there is need of a DSO
    }

    /**
     * Get the screen width: important for dynamically adjusting sizes
     *
     * @param a Needs an Activity to query the screen size
     * @return screen width in pixels
     */
    @SuppressWarnings("deprecation")
    protected int getScreenWidth(Activity a) {
        return a.getWindowManager().getDefaultDisplay().getWidth();
    }

}
