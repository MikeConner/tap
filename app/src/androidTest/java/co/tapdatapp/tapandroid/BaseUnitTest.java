/**
 * Various methods shared by Android unit tests
 */

package co.tapdatapp.tapandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.test.AndroidTestCase;

import java.nio.ByteBuffer;
import java.util.Random;

public class BaseUnitTest extends AndroidTestCase {

    protected final Random random = new Random();

    /**
     * Generate random binary data for testing
     * @return random length array of random bytes
     */
    protected byte[] randomData() {
        int length = random.nextInt(1024);
        byte[] rv = new byte[length];
        for (int i = 0; i < length; i++) {
            rv[i] = (byte) random.nextInt(255);
        }
        return rv;
    }

    /**
     * Return a byte array of the specified drawable, as long as it's
     * a Bitmap
     *
     * @param drawable
     * @return
     */
    protected byte[] getBitmapAsBytes(int drawable) {
        Bitmap b = BitmapFactory.decodeResource(TapApplication.get().getResources(), drawable);
        return getBitmapAsBytes(b);
    }

    protected byte[] getBitmapAsBytes(Bitmap b) {
        ByteBuffer buffer = ByteBuffer.allocate(b.getByteCount());
        b.copyPixelsToBuffer(buffer);
        return buffer.array();
    }
}
