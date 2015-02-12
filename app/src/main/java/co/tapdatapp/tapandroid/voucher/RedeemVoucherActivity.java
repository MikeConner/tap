/**
 * Redeem a voucher to charge up a currency
 */

package co.tapdatapp.tapandroid.voucher;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.helpers.TitleBar;

public class RedeemVoucherActivity extends Activity {

    TitleBar titleBar;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_redeem_voucher);
        titleBar = new TitleBar(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        titleBar.getAndSetBalance();
    }

    /**
     * ??
     *
     * @param v The button that was clicked
     */
    public void clickLoadVoucher(View v) {

    }
}
