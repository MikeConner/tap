/**
 * why Android doesn't implement the Files classes present in newer
 * versions of Java is beyond me. Just recreate some of it here.
 */

package co.tapdatapp.tapandroid.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class Files {

    public static byte[] readAllBytes(InputStream is) throws IOException {
        ArrayList<Byte> bytes = new ArrayList<>();
        int b;
        while ((b = is.read()) != -1) {
            bytes.add((byte)b);
        }
        int i = 0;
        byte[] rv = new byte[bytes.size()];
        for (byte current : bytes) {
            rv[i] = current;
            i++;
        }
        return rv;
    }

    public static byte[] readAllBytes(String path) throws IOException {
        File f = new File(path);
        long lSize = f.length();
        if (lSize > Integer.MAX_VALUE) {
            throw new AssertionError("file too large");
        }
        int size = (int)lSize;
        byte[] rv = new byte[size];
        FileInputStream is = null;
        try {
            is = new FileInputStream(path);
            int read = is.read(rv);
            if (read != size) {
                throw new AssertionError(
                    "Read " + read + " of file " + path + " size " + size
                );
            }
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException ioe) {
                    throw new RuntimeException(ioe);
                }
            }
        }
        return rv;
    }

}
