package co.tapdatapp.tapandroid.history;

/**
 * Created by Vince on 2/11/2015.
 */
public interface HistorySyncCallback {

    public void fillInList();

    public void postSyncDisplay();

    public void syncFailure(Exception cause);
}
