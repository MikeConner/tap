/**
 * List the currencies this user has purchased, and the balance of
 * each currency. Allow the user to select the default currency.
 */

package co.tapdatapp.tapandroid.currency;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.localdata.UserBalance;
import co.tapdatapp.tapandroid.user.Account;

public class BalancesActivity
extends Activity
implements AdapterView.OnItemClickListener, GetAllBalancesTask.Callback {

    private ListView balanceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balances);
        balanceList = (ListView)findViewById(R.id.balances_list);
        balanceList.setOnItemClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillInList();
    }

    /**
     * Fill in the list with balances
     */
    private void fillInList() {
        findViewById(R.id.balances_progress_bar).setVisibility(View.VISIBLE);
        balanceList.setVisibility(View.GONE);
        new GetAllBalancesTask().execute(this);
    }

    /**
     * Callback from GetAllBalancesTask once all balances are loaded.
     * Update the UI from here to actually display the balances.
     *
     * @param list List of Currency ID -> balance mappings
     */
    public void onBalancesLoaded(BalanceList list) {
        BalanceListAdapter adapter = new BalanceListAdapter(
            this,
            new UserBalance(),
            list
        );
        balanceList.setAdapter(adapter);
        findViewById(R.id.balances_progress_bar).setVisibility(View.GONE);
        balanceList.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_balances, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        if (l > Integer.MAX_VALUE) {
            throw new AssertionError("Currency ID exceeds int size");
        }
        new Account().setActiveCurrency((int) l);
        finish();
    }
}
