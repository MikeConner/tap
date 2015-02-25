/**
 * Bridge between the ListView of the balances screen and the data
 * in the database/cache/web
 */

package co.tapdatapp.tapandroid.currency;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;
import co.tapdatapp.tapandroid.helpers.TapBitmap;
import co.tapdatapp.tapandroid.localdata.BaseAdapter;
import co.tapdatapp.tapandroid.localdata.CurrencyDAO;

public class BalanceListAdapter extends BaseAdapter {

    private final CurrencyDAO userBalance;
    private final BalanceList balanceList;
    private Bitmap loadingImage;
    private Activity activity;
    private int imageSize;

    public BalanceListAdapter(Activity a,
                              CurrencyDAO dao,
                              BalanceList list
    ) {
        activity = a;
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
        // Note that recycling Views confuses things and causes images
        // to be applied to the wrong rows by the background task
        LayoutInflater inflater =
            (LayoutInflater) TapApplication.get().getSystemService(
                Context.LAYOUT_INFLATER_SERVICE
            );
        view = inflater.inflate(
            R.layout.line_item_balance,
            viewGroup,
            false
        );
        CurrencyDAO currency = userBalance.getByNameOrder(i);
        ((TextView)view.findViewById(R.id.balance_line_item_balance)).setText(
            currency.getSymbol() +
                balanceList.get(currency.getCurrencyId()).toString() +
                " " + currency.getName()
        );
        imageSize = getScreenWidth(activity) / 7;
        final ImageView imageView = (ImageView)view.findViewById(R.id.balance_line_item_icon);
        if (currency.getCurrencyId() == CurrencyDAO.CURRENCY_BITCOIN) {
            imageView.setImageBitmap(
                Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(
                        activity.getResources(),
                        R.drawable.bitcoin_icon
                    ),
                    imageSize,
                    imageSize,
                    true
                )
            );
        }
        else {
            imageView.setImageBitmap(getLoadingImage());
            new SetBalanceImageTask().execute(
                imageView,
                currency.getIconUrl(),
                imageSize
            );
        }
        return view;
    }

    @Override
    public int getItemViewType(int i) {
        return R.layout.line_item_balance;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return getCount() != 0;
    }

    private Bitmap getLoadingImage() {
        if (loadingImage == null) {
            loadingImage = TapBitmap.getLoadingBitmapAtSize(imageSize);
        }
        else {
            if (loadingImage.getWidth() != imageSize) {
                loadingImage = TapBitmap.getLoadingBitmapAtSize(imageSize);
            }
        }
        return loadingImage;
    }

}
