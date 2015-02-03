package co.tapdatapp.tapandroid.localdata;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.MoreAsserts;

import java.io.ByteArrayOutputStream;
import java.util.NoSuchElementException;

import co.tapdatapp.tapandroid.BaseUnitTest;
import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;

public class DenominationTest extends BaseUnitTest {

    public void testStorage() {
        final int currencyId = random.nextInt(999);
        final int amount = random.nextInt(999);
        final String image = "http://www.example.com/testStorage.png";
        Denomination d = new Denomination(currencyId, amount, image);
        d.create();
        d = new Denomination();
        try {
            assertEquals(
                "Denomination not saved properly",
                image,
                d.getURL(currencyId, amount)
            );
        }
        finally {
            d.removeAll(currencyId);
        }
    }

    public void testRemoveAll() {
        final int currencyId = random.nextInt(999);
        final int amount = random.nextInt(999);
        final String image = "http://www.example.com/testStorage.png";
        Denomination d = new Denomination(currencyId, amount, image);
        d.create();
        d = new Denomination();
        d.removeAll(currencyId);
        try {
            d.getURL(currencyId, amount);
            fail("Should have thrown NoSuchElementException");
        }
        catch (NoSuchElementException nsee) {
            // Expected behavior
        }
    }

    public void testGetBitmapFromCache() throws Exception {
        final int currencyId = random.nextInt(999);
        final int amount = random.nextInt(999);
        final String image = "http://www.example.com/testGetBitmapFromCache.png";
        Denomination d = new Denomination(currencyId, amount, image);
        d.create();
        AndroidCache c = new AndroidCache();
        Bitmap bitmap = BitmapFactory.decodeResource(TapApplication.get().getResources(), R.drawable.loading_square);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        c.put(image, "image/png", os.toByteArray());
        os.close();
        byte[] expectedImage = getBitmapAsBytes(R.drawable.loading_square);
        try {
            byte[] result = getBitmapAsBytes(d.getBitmap(currencyId, amount));
            MoreAsserts.assertEquals("Images don't match", expectedImage, result);
        }
        finally {
            c.remove(image);
            d.removeAll(currencyId);
        }
    }
}
