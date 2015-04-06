/**
 * Factory for getting the appropriate driver for storing image,
 * video, etc data.
 */

package co.tapdatapp.tapandroid.remotedata;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.helpers.DevHelper;

public class RemoteStorage {

    public static RemoteStorageDriver getDriver() {
        if (DevHelper.isEnabled(R.string.USE_AWS)) {
            return new AmazonStorage();
        }
        else {
            return new DevStorage();
        }
    }
}
