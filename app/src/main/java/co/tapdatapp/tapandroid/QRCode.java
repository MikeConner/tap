package co.tapdatapp.tapandroid;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import co.tapdatapp.tapandroid.user.Account;

/**
 * Created by Vince on 2/27/2015.
 */
public class QRCode extends Activity {

    public void onCreate(Bundle state){
        super.onCreate(state);
        setContentView(R.layout.fragment_deposit_btc);

        Account account = new Account();
        TextView authToken = (TextView) findViewById(R.id.txtBtcInbound);

        authToken.setText(account.getAuthToken());
    }
}
