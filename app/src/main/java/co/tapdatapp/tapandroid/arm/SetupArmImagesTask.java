/**
 * background task to fetch any images from the server as required, and
 * set up the ArmFragment
 */

package co.tapdatapp.tapandroid.arm;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.localdata.Denomination;
import co.tapdatapp.tapandroid.user.Account;

public class SetupArmImagesTask extends AsyncTask<ArmFragment, Void, Void> {

    private ArmFragment armFragment;
    private Denomination[] denominations;
    private Bitmap[] bitmaps;
    private Bitmap icon;

    /**
     * Fetch all currency details and all bitmaps associated with
     * the currency and save them so the postexecute task can pass
     * them back to the UI for display.
     *
     * @param params Just the Fragment to communicate back to
     * @return nothing
     */
    @Override
    protected Void doInBackground(ArmFragment... params) {
        if (params.length < 1) {
            throw new AssertionError("Must provide the Fragment");
        }
        armFragment = params[0];
        CurrencyDAO currency = new CurrencyDAO();
        Account account = new Account();
        try {
            currency.ensureLocalCurrencyDetails(account.getActiveCurrency());
            icon = TapBitmap.fetchFromCacheOrWeb(currency.getIconUrl());
        }
        catch (Exception e) {
            // @TODO substitute a default image here
        }
        denominations = currency.getDenominations(
            account.getActiveCurrency()
        );
        bitmaps = new Bitmap[denominations.length];
        for (int i = 0; i < denominations.length; i++) {
            try {
                bitmaps[i] = TapBitmap.fetchFromCacheOrWeb(
                    denominations[i].getURL()
                );
            }
            catch (Exception e) {
                // @TODO substitute a default image here
            }
        }
        return null;
    }

    /**
     * Just pass the gathered data back to the UI object
     *
     * @param v nothing
     */
    @Override
    protected void onPostExecute(Void v) {
        armFragment.updateDenominations(denominations, bitmaps, icon);
    }
}
