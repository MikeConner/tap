/**
 * Phony Currency data adapter. Has a hard-coded list of currencies
 * for testing.
 */

package co.tapdatapp.tapandroid.localdata;

import android.graphics.Bitmap;

import co.tapdatapp.tapandroid.currency.BalanceList;

public class MockCurrency implements CurrencyDAO {

    private int currentLocation = 0;

    @Override
    public void moveTo(int id) {
        currentLocation = id;
    }

    @Override
    public CurrencyDAO getByNameOrder(int index) {
        MockCurrency rv = new MockCurrency();
        rv.currentLocation = index;
        return rv;
    }

    @Override
    public Bitmap getIcon() throws Exception {
        throw new NoSuchMethodError("Not implemented");
    }

    @Override
    public int getCurrencyId() {
        return currencies[currentLocation].currencyId;
    }

    @Override
    public String getName() {
        return currencies[currentLocation].name;
    }

    @Override
    public String getSymbol() {
        return currencies[currentLocation].symbol;
    }

    public String getSymbol(int currencyId) {
        if (currencyId == CurrencyDAO.CURRENCY_BITCOIN) {
            return "S";
        }
        return currencies[currencyId].symbol;
    }

    @Override
    public String getIconUrl() {
        return currencies[currentLocation].icon;
    }

    @Override
    public BalanceList getAllBalances() {
        BalanceList rv = new BalanceList();
        rv.put(0, 23);
        rv.put(1, 123);
        rv.put(2, 223);
        rv.put(3, 323);
        rv.put(4, 423);
        rv.put(5, 523);
        rv.put(6, 623);
        rv.put(7, 723);
        rv.put(8, 823);
        rv.put(9, 923);
        return rv;
    }

    @Override
    public int getBalance(int currencyId) {
        if (currencyId == CurrencyDAO.CURRENCY_BITCOIN) {
            return 5432;
        }
        return getAllBalances().get(currencyId);
    }

    @Override
    public String getBalanceAsString(int currencyId) {
        return getSymbol(currencyId) + Integer.toString(getBalance(currencyId));
    }

    @Override
    public int getMaxPayout(int currencyId) {
        return 500;
    }

    public static class CurrencyHolder {

        public int currencyId;
        public String name;
        public String symbol;
        public String icon;

        CurrencyHolder(int i, String n, String s, String _icon) {
            currencyId = i;
            name = n;
            symbol = s;
            icon = _icon;
        }

    }

    private final static CurrencyHolder[] currencies;

    static {
        currencies = new CurrencyHolder[10];
        currencies[0] = new CurrencyHolder(0, "Bucky Bucks", "B", "http://www.example.com/0.jpg");
        currencies[3] = new CurrencyHolder(1, "Crazy Credits", "C", "http://www.example.com/3.jpg");
        currencies[1] = new CurrencyHolder(2, "Ducky Dollars", "$", "http://www.example.com/1.jpg");
        currencies[5] = new CurrencyHolder(3, "Euros", "\u20ac", "http://www.example.com/5.jpg");
        currencies[2] = new CurrencyHolder(4, "Fun Bucks", "F", "http://www.example.com/2.jpg");
        currencies[4] = new CurrencyHolder(5, "Marks", "M", "http://www.example.com/4.jpg");
        currencies[6] = new CurrencyHolder(6, "Mine Dollars", "E", "http://www.example.com/6.jpg");
        currencies[7] = new CurrencyHolder(7, "Monies", "M", "http://www.example.com/7.jpg");
        currencies[8] = new CurrencyHolder(8, "Pounds", "\u00a3", "http://www.example.com/8.jpg");
        currencies[9] = new CurrencyHolder(9, "Yen", "\u00a5", "http://www.example.com/9.jpg");
    }
}
