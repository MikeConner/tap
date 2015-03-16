/**
 * Interface for data access to currencies
 */

package co.tapdatapp.tapandroid.localdata;

import android.graphics.Bitmap;

import co.tapdatapp.tapandroid.currency.BalanceList;
import co.tapdatapp.tapandroid.remotedata.WebServiceError;

public interface CurrencyDAO {

    public static final int CURRENCY_BITCOIN = -1;
    public static final String BITCOIN_SYMBOL = "S";
    public static final int BITCOIN_MAX_TAP = 500;

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
     * @param currencyId Pass a currency ID to get the symbol for it
     * @return Currency symbol for the specified currency (such as "$")
     */
    String getSymbol(int currencyId);

    /**
     * @return The URL from which to fetch this currency's icon image
     */
    String getIconUrl();

    /**
     * Get the list of currency IDs and their balance.
     *
     * @return  BalanceList
     */
    BalanceList getAllBalances() throws WebServiceError;

    /**
     * @param currencyId Currency ID
     * @return the balance of that Currency ID
     */
    int getBalance(int currencyId) throws WebServiceError;

    /**
     * @return String of the currency symbol + balance
     */
    String getBalanceAsString(int currencyId) throws WebServiceError;

    /**
     * Return the maximum amount that a single payout can be. This is
     * a per-currency value
     *
     * @param currencyId Currency ID to get the value for
     * @return Maximum allowed single payout
     */
    int getMaxPayout(int currencyId);

    /**
     * Ensure that the provided currency has all of its details in
     * the local database.
     *
     * @param currencyId Currency ID to operate on
     */
    void ensureLocalCurrencyDetails(int currencyId) throws WebServiceError;

    /**
     * Return all the denominations (in ascending order) for the
     * requested currency
     *
     * @param currencyId
     * @return List of Denominations
     */
    Denomination[] getDenominations(int currencyId);
}
