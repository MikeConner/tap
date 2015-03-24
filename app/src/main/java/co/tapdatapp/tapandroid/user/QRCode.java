/**
 * Screen to display a QR code and the bitcoin address to use to
 * send money to this account.
 */

package co.tapdatapp.tapandroid.user;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.helpers.TapBitmap;

public class QRCode extends Activity implements TapBitmap.Callback {

    public void onCreate(Bundle state){
        super.onCreate(state);
        setContentView(R.layout.fragment_deposit_btc);

        Account account = new Account();
        TextView bitcoinAddress = (TextView) findViewById(R.id.txtBtcInbound);

        bitcoinAddress.setText(account.getBitcoinAddress());

        new TapBitmap().execute(this, account.getBitcoinQrUrl());
    }

    @Override
    public void onImageRetrieved(Bitmap image) {
        try {
            ((ImageView) findViewById(R.id.bitQR)).setImageBitmap(image);
        }
        catch (NullPointerException npe) {
            // This can happen if the user navigates away from the
            // Activity faster than the background task can finish,
            // and can be ignored
            Log.e("IGNORED", "NPE in callback", npe);
        }
    }
}
