/**
 * Allows multiple drivers for storage of images/videos/etc
 */

package co.tapdatapp.tapandroid.remotedata;

public interface RemoteStorageDriver {
    /**
     * Store the specified byte array
     *
     * @param data The data to store
     * @param type the content type of the data, will be guessed if null
     * @return URL to access the data
     */
    String store(byte[] data, String type) throws Exception;

    /**
     * Overwrite the data with the provided data
     *
     * @param url Where the data is stored
     * @param data The new data
     */
    void overWrite(String url, byte[] data) throws Exception;
}
