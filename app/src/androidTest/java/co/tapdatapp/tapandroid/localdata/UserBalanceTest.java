package co.tapdatapp.tapandroid.localdata;

import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.test.MoreAsserts;

import org.junit.After;
import org.junit.Before;

import java.io.ByteArrayOutputStream;
import java.util.NoSuchElementException;

import co.tapdatapp.tapandroid.BaseUnitTest;
import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;

public class UserBalanceTest extends BaseUnitTest {


    @Before
    public void setUp() {
        removeAllRecords();
    }

    @After
    public void tearDown() {
        removeAllRecords();
    }

    /**
     * Ensure there is nothing in the balance/denominations tables
     * prior and after each test.
     */
    private void removeAllRecords() {
        SQLiteDatabase db = new DatabaseHelper(TapApplication.get()).getWritableDatabase();
        db.delete(Denomination.TABLE, null, null);
        db.delete(UserBalance.TABLE, null, null);
    }

    /**
     * Create a new currency
     */
    public void testCreateOrUpdate_create() {
        UserBalance b = new UserBalance();
        final int id = random.nextInt(999);
        final String name = "testCreateOrUpdate_create";
        final String icon = "http://www.example.com/image.png";
        final String symbol = "$";
        final int maxTap = 5;
        b.createOrUpdate(id, name, icon, symbol, maxTap);
        b = new UserBalance();
        b.moveTo(id);
        assertEquals("name did not match", name, b.getName());
        assertEquals("icon URL did not match", icon, b.getIconUrl());
        assertEquals("symbol did not match", symbol, b.getSymbol());
    }

    /**
     * Update an existing currency
     */
    public void testCreateOrUpdate_update() {
        UserBalance b = new UserBalance();
        final int id = random.nextInt(999);
        final String name0 = "testCreateOrUpdate_create";
        final String icon0 = "http://www.example.com/image.png";
        final String symbol0 = "$";
        final String name1 = "testCreateOrUpdate_update";
        final String icon1 = "http://www.example.com/newImage.png";
        final String symbol1 = "%";
        b.createOrUpdate(id, name0, icon0, symbol0, 0);
        b = new UserBalance();
        b.createOrUpdate(id, name1, icon1, symbol1, 0);
        b = new UserBalance();
        b.moveTo(id);
        assertEquals("name did not match", name1, b.getName());
        assertEquals("icon URL did not match", icon1, b.getIconUrl());
        assertEquals("symbol did not match", symbol1, b.getSymbol());
    }

    public void testUpdateAllDenominations_create() {
        UserBalance b = new UserBalance();
        final int id = random.nextInt(999);
        final String name = "testUpdateAllDenominations_create";
        final String icon = "http://www.example.com/image.png";
        final String symbol = "$";
        Denomination[] d = new Denomination[2];
        d[0] = new Denomination(id, 1, "d1image");
        d[1] = new Denomination(id, 5, "d5image");
        b.createOrUpdateAll(id, name, icon, symbol, 0, d);
        b = new UserBalance();
        b.moveTo(id);
        assertEquals("name did not match", name, b.getName());
        assertEquals("icon URL did not match", icon, b.getIconUrl());
        assertEquals("symbol did not match", symbol, b.getSymbol());
        Denomination denomination = new Denomination();
        assertEquals("$1 did not match", "d1image", denomination.getURL(id, 1));
        assertEquals("$1 did not match", "d5image", denomination.getURL(id, 5));
    }

    public void testUpdateAllDenominations_deleteDenominations() {
        UserBalance b = new UserBalance();
        final int id = random.nextInt(999);
        final String name = "testUpdateAllDenominations_deleteDenominations";
        final String icon = "http://www.example.com/image.png";
        final String symbol = "$";
        Denomination[] d = new Denomination[2];
        d[0] = new Denomination(id, 1, "d1image");
        d[1] = new Denomination(id, 5, "d5image");
        b.createOrUpdateAll(id, name, icon, symbol, 0, d);
        b = new UserBalance();
        b.createOrUpdateAll(id, name, icon, symbol, 0, null);
        b = new UserBalance();
        b.moveTo(id);
        assertEquals("name did not match", name, b.getName());
        assertEquals("icon URL did not match", icon, b.getIconUrl());
        assertEquals("symbol did not match", symbol, b.getSymbol());
        Denomination denomination = new Denomination();
        try {
            denomination.getURL(id, 1);
            fail("Should have thrown NoSuchElementException");
        }
        catch (NoSuchElementException nse) {
            // Correct behavior
        }
        try {
            denomination.getURL(id, 5);
            fail("Should have thrown NoSuchElementException");
        }
        catch (NoSuchElementException nse) {
            // Correct behavior
        }
    }

    public void testUpdateAllDenominations_updateDenominations() {
        UserBalance b = new UserBalance();
        final int id = random.nextInt(999);
        final String name = "testUpdateAllDenominations_deleteDenominations";
        final String icon = "http://www.example.com/image.png";
        final String symbol = "$";
        Denomination[] d = new Denomination[2];
        d[0] = new Denomination(id, 1, "d1image");
        d[1] = new Denomination(id, 5, "d5image");
        b.createOrUpdateAll(id, name, icon, symbol, 0, d);
        b = new UserBalance();
        d[0] = new Denomination(id, 1, "d1newImage");
        d[1] = new Denomination(id, 5, "d5newImage");
        b.createOrUpdateAll(id, name, icon, symbol, 0, d);
        b = new UserBalance();
        b.moveTo(id);
        assertEquals("name did not match", name, b.getName());
        assertEquals("icon URL did not match", icon, b.getIconUrl());
        assertEquals("symbol did not match", symbol, b.getSymbol());
        Denomination denomination = new Denomination();
        assertEquals("$1 did not match", "d1newImage", denomination.getURL(id, 1));
        assertEquals("$1 did not match", "d5newImage", denomination.getURL(id, 5));
    }

    public void testGetIconFromCache() throws Exception {
        final int id = random.nextInt(999);
        final String name = "testUpdateAllDenominations_deleteDenominations";
        final String icon = "http://www.example.com/testGetIconFromCache.png";
        final String symbol = "$";
        UserBalance b = new UserBalance();
        b.createOrUpdate(id, name, icon, symbol, 0);
        AndroidCache c = new AndroidCache();
        try {
            Bitmap bitmap = BitmapFactory.decodeResource(
                TapApplication.get().getResources(),
                R.drawable.loading_square
            );
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            c.put(icon, "image/png", os.toByteArray());
            os.close();
            byte[] expectedImage = getBitmapAsBytes(R.drawable.loading_square);
            b = new UserBalance();
            b.moveTo(id);
            byte[] result = getBitmapAsBytes(b.getIcon());
            MoreAsserts.assertEquals(
                "Icons don't match",
                expectedImage,
                result
            );
        }
        finally {
            c.remove(icon);
        }
    }

}
