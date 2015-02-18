/**
 * Callback from HistorySyncTask to return values or errors to the UI
 */

package co.tapdatapp.tapandroid.history;

public interface HistorySyncCallback {

    public void postSyncDisplay();

    public void syncFailure(Exception cause);
}
