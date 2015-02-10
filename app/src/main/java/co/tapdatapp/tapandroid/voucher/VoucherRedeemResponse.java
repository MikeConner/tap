/**
 * Object holding the response to a voucher redemption request
 */

package co.tapdatapp.tapandroid.voucher;

public class VoucherRedeemResponse {
    private int balance;
    private int amountRedeemed;
    private int currencyId;

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getAmountRedeemed() {
        return amountRedeemed;
    }

    public void setAmountRedeemed(int amountRedeemed) {
        this.amountRedeemed = amountRedeemed;
    }

    public int getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(int currencyId) {
        this.currencyId = currencyId;
    }
}
