/**
 * Methods for the common title bar
 */

package co.tapdatapp.tapandroid.helpers;

import android.app.Activity;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.remotedata.GetCurrentBalanceAsStringTask;
import co.tapdatapp.tapandroid.user.Account;

public class TitleBar implements GetCurrentBalanceAsStringTask.Callback {

    private TextView balance;
    private Account account;

    public TitleBar(Activity titleBarView) {
        super();
        account = new Account();
        balance = (TextView)titleBarView.findViewById(R.id.title_bar_balance);
    }

    public void getAndSetBalance() {
        if (!account.created()) {
            throw new AssertionError("Can't display title bar without an account");
        }
        new GetCurrentBalanceAsStringTask().execute(this);
    }

    public void setBalance(String s) {
        balance.setText(s);
    }

}
