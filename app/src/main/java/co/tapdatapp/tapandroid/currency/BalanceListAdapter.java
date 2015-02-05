/**
 * Bridge between the ListView of the balances screen and the data
 * in the database/cache/web
 */

package co.tapdatapp.tapandroid.currency;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;

public class BalanceListAdapter implements ListAdapter {

    private final CurrencyDAO userBalance;
    private final BalanceList balanceList;

    public BalanceListAdapter(CurrencyDAO dao, BalanceList list) {
        balanceList = list;
        userBalance = dao;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int i) {
        return true;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver dataSetObserver) {
        // The dataset is static. When it changes a new adapter is
        // created, so DSOs don't need to be tracked.
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver dataSetObserver) {
        // The dataset is static. When it changes a new adapter is
        // created, so DSOs don't need to be tracked.
    }

    @Override
    public int getCount() {
        return balanceList.size();
    }

    @Override
    public CurrencyDAO getItem(int i) {
        return userBalance.getByNameOrder(i);
    }

    @Override
    public long getItemId(int i) {
        return userBalance.getByNameOrder(i).getCurrencyId();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater inflater =
                (LayoutInflater) TapApplication.get().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE
                );
            view = inflater.inflate(
                R.layout.balance_line_item,
                viewGroup,
                false
            );
        }
        CurrencyDAO currency = userBalance.getByNameOrder(i);
        ((TextView)view.findViewById(R.id.balance_line_item_name)).setText(currency.getName());
        ((TextView)view.findViewById(R.id.balance_line_item_balance)).setText(
            currency.getSymbol() + balanceList.get(currency.getCurrencyId()).toString()
        );
        // @TODO set loading image and background fetch to real image
        //((ImageView)view.findViewById(R.id.balance_line_item_icon)).setImageBitmap(R.drawable.loading_square);
        return view;
    }

    @Override
    public int getItemViewType(int i) {
        return R.layout.balance_line_item;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getCount() != 0;
    }

}
