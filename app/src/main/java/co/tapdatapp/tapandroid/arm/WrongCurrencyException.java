/**
 * Thrown when a transaction is attempted with the wrong currency
 */

package co.tapdatapp.tapandroid.arm;

import co.tapdatapp.tapandroid.remotedata.WebResponse;
import co.tapdatapp.tapandroid.remotedata.WebServiceError;

public class WrongCurrencyException extends WebServiceError {

    private int correctCurrency;

    public WrongCurrencyException(WebResponse wr, int currencyId) {
        super(wr);
        correctCurrency = currencyId;
    }

    public int getCorrectCurrency() {
        return correctCurrency;
    }

}
