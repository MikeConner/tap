package co.tapdatapp.tapandroid.user;

import co.tapdatapp.tapandroid.BaseUnitTest;

public class AccountTest extends BaseUnitTest {

    /**
     * Test setting and retrieving the active currency
     */
    public void testCurrency() {
        Account a = new Account();
        final int startValue = a.getActiveCurrency();
        int testValue = startValue;
        while (testValue == startValue) {
            testValue = random.nextInt();
        }
        try {
            a.setActiveCurrency(testValue);
            assertEquals(
                "Active Currency was not changed",
                testValue,
                a.getActiveCurrency()
            );
        }
        finally {
            a.setActiveCurrency(startValue);
        }
    }

    /**
     * If Account is to successfully avoid being a singleton, this has
     * to pass. It tests that changing the value on one object is
     * immediately visible to another object.
     */
    public void testConcurrentObjects() {
        Account a1 = new Account();
        Account a2 = new Account();
        final int startValue = a1.getActiveCurrency();
        assertEquals(
            "Both objects should have the same result",
            a1.getActiveCurrency(),
            a2.getActiveCurrency()
        );
        int testValue = startValue;
        while (testValue == startValue) {
            testValue = random.nextInt();
        }
        try {
            a1.setActiveCurrency(testValue);
            assertEquals(
                "Active Currency was not changed",
                testValue,
                a2.getActiveCurrency()
            );
        }
        finally {
            a1.setActiveCurrency(startValue);
        }
    }

}
