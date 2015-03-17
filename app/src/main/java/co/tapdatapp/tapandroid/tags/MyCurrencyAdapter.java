/**
 * SpinnerAdapter for selecting one of the currencies owned by the user
 */

package co.tapdatapp.tapandroid.tags;

import android.app.Activity;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.LinkedHashMap;

import co.tapdatapp.tapandroid.localdata.CurrencyDAO;

public class MyCurrencyAdapter implements SpinnerAdapter {

    private final LinkedHashMap<Integer, String> currencyList;
    private final int[] order;
    private final Activity activity;

    public MyCurrencyAdapter(Activity a) {
        activity = a;
        CurrencyDAO dao = new CurrencyDAO();
        currencyList = dao.getAllOwnedCurrencies();
        order = new int[currencyList.size()];
        int i = 0;
        for (Integer id : currencyList.keySet()) {
            order[i] = id;
            i++;
        }
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return null;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        // Data in dataset doesn't change
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        // Data in dataset doesn't change
    }

    @Override
    public int getCount() {
        return currencyList.size();
    }

    @Override
    public Object getItem(int position) {
        return currencyList.get((int)getItemId(position));
    }

    @Override
    public long getItemId(int position) {
        return order[position];
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getView(int p, View view, ViewGroup parent) {
        if (view == null) {
            LayoutInflater inflater = activity.getLayoutInflater();
            view = inflater.inflate(android.R.layout.simple_spinner_dropdown_item, null);
        }
        ((TextView)view.findViewById(android.R.id.text1)).setText((String)getItem(p));
        return view;
    }

    @Override
    public int getItemViewType(int position) {
        return android.R.layout.simple_spinner_dropdown_item;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getCount() == 0;
    }
}
