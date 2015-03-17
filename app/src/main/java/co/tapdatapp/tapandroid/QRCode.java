/**
 * Screen to display a QR code and the bitcoin address to use to
 * send money to this account.
 */

package co.tapdatapp.tapandroid;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.helpers.UserFriendlyError;
import co.tapdatapp.tapandroid.user.Account;

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
        ((ImageView)findViewById(R.id.bitQR)).setImageBitmap(image);
    }

    /**
     * Now gets called when there's an error retrieving the image
     * @param t The error
     */
    @Override
    public void onImageRetrievalError(Throwable t) {
        try{
            throw t;
        }
        catch (UserFriendlyError ufe){
            TapApplication.errorToUser(ufe);
        }
        catch (Throwable catchall) {
            TapApplication.unknownFailure(t);
        }
    }
}
