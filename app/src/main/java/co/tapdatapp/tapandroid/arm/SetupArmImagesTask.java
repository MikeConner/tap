/**
 * background task to fetch any images from the server as required, and
 * set up the ArmFragment
 */

package co.tapdatapp.tapandroid.arm;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;
import co.tapdatapp.tapandroid.localdata.Denomination;
import co.tapdatapp.tapandroid.remotedata.WebServiceError;
import co.tapdatapp.tapandroid.user.Account;

public class SetupArmImagesTask extends AsyncTask<ArmFragment, Void, Void> {

    private ArmFragment armFragment;
    private Denomination[] denominations;
    private Bitmap[] bitmaps;
    private Bitmap icon;
    private Throwable error = null;

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
        int currencyId = account.getActiveCurrency();
        try {
            currency.ensureLocalCurrencyDetails(currencyId);
            currency.moveTo(currencyId);
        }
        catch (WebServiceError wse) {
            error = wse;
            return null;
        }
        try {
            icon = TapBitmap.fetchFromCacheOrWeb(currency.getIconUrl());
        }
        catch (Exception e) {
            // if we get an exception, assume the currency data is
            // stale and force a reload
            try {
                currency.syncCurrencyWithServer(currencyId);
                icon = TapBitmap.fetchFromCacheOrWeb(currency.getIconUrl());
            }
            catch (Exception e0) {
                error = e0;
                return null;
            }
        }
        denominations = currency.getDenominations(currencyId);
        try {
            getBitmaps();
        }
        catch (Exception e) {
            // if we get an exception, assume the currency data is
            // stale and force a reload
            try {
                currency.syncCurrencyWithServer(currencyId);
                getBitmaps();
            }
            catch (Exception e0) {
                error = e0;
                return null;
            }
        }
        return null;
    }

    private void getBitmaps() throws Exception {
        bitmaps = new Bitmap[denominations.length];
        for (int i = 0; i < denominations.length; i++) {
            bitmaps[i] = TapBitmap.fetchFromCacheOrWeb(
                denominations[i].getURL()
            );
        }
    }

    /**
     * Just pass the gathered data back to the UI object
     *
     * @param v nothing
     */
    @Override
    protected void onPostExecute(Void v) {
        if (error != null) {
            armFragment.armImagesFailure(error);
        }
        else {
            armFragment.updateDenominations(denominations, bitmaps, icon);
        }
    }
}
