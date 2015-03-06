/**
 * An exception that might include an error message safe to display
 * to the user.
 */

package co.tapdatapp.tapandroid.helpers;

import co.tapdatapp.tapandroid.R;
import co.tapdatapp.tapandroid.TapApplication;

public class UserFriendlyError extends Exception {

    private String userError = null;

    /**
     * @return true if this exception includes an error that can be shown to the user
     */
    public boolean hasUserError() {
        return userError != null && !userError.isEmpty();
    }

    /**
     * Set the user-displayable message
     */
    public void setUserError(String to) {
        userError = to;
    }

    /**
     * @return an error message safe to show the user
     */
    public String getUserError() {
        if (hasUserError()) {
            return userError;
        }
        else {
            return TapApplication.string(R.string.unknown_error);
        }
    }
}
