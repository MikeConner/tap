package co.tapdatapp.tapandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.user.Account;

public class QRCode extends Activity {

    public void onCreate(Bundle state){
        super.onCreate(state);
        setContentView(R.layout.fragment_deposit_btc);

        Account account = new Account();
        Bitmap imgBitmap = null;
        TextView bitcoinAddress = (TextView) findViewById(R.id.txtBtcInbound);
        ImageView bitQR = (ImageView) findViewById(R.id.bitQR);

        /**
         * I'm pretty sure that I'm not getting data from the server correctly which is what's causing these problems.
         */
        bitcoinAddress.setText(account.getBitcoinAddress());

        try {
           imgBitmap  = TapBitmap.fetchFromCacheOrWeb(account.getBitcoinQrUrl());
        }
        catch (Exception e) {
            TapApplication.unknownFailure(e);
        }

        bitQR.setImageBitmap(imgBitmap);

    }
}
