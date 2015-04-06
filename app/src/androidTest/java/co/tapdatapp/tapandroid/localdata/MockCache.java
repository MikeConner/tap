/**
 * A mock implementation of the Cache interface for testing.
 */

package co.tapdatapp.tapandroid.localdata;

import java.io.InputStream;
import java.util.NoSuchElementException;

public class MockCache implements Cache {

    byte[] putData;
    String typeData;
    String nameData;
    String removedName;
    int totalSize;
    String oldestName;
    long lastAccessed;

    @Override
    public void put(String name, String mediaType, byte[] data) {
        nameData = name;
        typeData = mediaType;
        putData = data;
    }

    @Override
    public void put(String name, String mediaType, InputStream data) {
        throw new NoSuchMethodError("Not implemented");
    }

    @Override
    public String getType(String name) {
        return typeData;
    }

    @Override
    public byte[] get(String name) {
        if (!name.equals(nameData)) {
            throw new NoSuchElementException(name);
        }
        return putData;
    }

    @Override
    public InputStream getStream(String name) {
        throw new NoSuchMethodError("Not implemented");
    }

    @Override
    public void remove(String name) {
        removedName = name;
    }

    @Override
    public int getTotalSize() {
        return totalSize;
    }

    @Override
    public String getOldest() {
        return oldestName;
    }

    @Override
    public long getLastAccessed(String name) {
        return lastAccessed;
    }
}
