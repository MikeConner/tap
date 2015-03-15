/**
 * Allows multiple drivers for storage of images/videos/etc
 */

package co.tapdatapp.tapandroid.remotedata;

public interface RemoteStorageDriver {
    /**
     * Store the specified byte array
     *
     * @param data The data to store
     * @return URL to access the data
     */
    String store(byte[] data) throws Exception;
}
