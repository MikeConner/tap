/**
 * Interface for data access to currencies
 */

package co.tapdatapp.tapandroid.localdata;

import android.graphics.Bitmap;

import co.tapdatapp.tapandroid.currency.BalanceList;

public interface CurrencyDAO {

    public static final int CURRENCY_BITCOIN = -1;

    /**
     * Load this class up with the parameters from the specified
     * currency ID
     *
     * @param id Currency ID
     */
    void moveTo(int id);

    /**
     * Ordering the currencies by the name field, return #index
     *
     * @param index Number of the currency when ordered by name
     * @return a loaded CurrencyDAO object
     */
    CurrencyDAO getByNameOrder(int index);

    /**
     * Retrieve an actual Bitmap of the currency icon
     *
     * @return Bitmap of the currency icon
     * @throws Exception if the file can't be accessed
     */
    Bitmap getIcon() throws Exception;

    /**
     * @return the currency ID of this currency
     */
    int getCurrencyId();

    /**
     * @return the display name of the currency
     */
    String getName();

    /**
     * @return The currency symbol (such as "$")
     */
    String getSymbol();

    /**
     * @return The URL from which to fetch this currency's icon image
     */
    String getIconUrl();

    /**
     * Get the list of currency IDs and their balance.
     *
     * @return  BalanceList
     */
    BalanceList getAllBalances();

    /**
     * @param currencyId Currency ID
     * @return the balance of that Currency ID
     */
    int getBalance(int currencyId);

    /**
     * @return String of the currency symbol + balance
     */
    String getBalanceAsString(int currencyId);
}
