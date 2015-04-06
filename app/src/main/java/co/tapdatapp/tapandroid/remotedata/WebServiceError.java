/**
 * Carrier class to encapsulate exceptions thrown by the web service
 */

package co.tapdatapp.tapandroid.remotedata;

import co.tapdatapp.tapandroid.helpers.UserFriendlyError;

public class WebServiceError extends UserFriendlyError {

    private WebResponse webResponse = null;

    /**
     * Constructor for when a response was actually received, but was
     * in error for some reason.
     *
     * @param wr Received response
     */
    public WebServiceError(WebResponse wr) {
        super();
        webResponse = wr;
        setUserError(wr.getUserError());
    }

    /**
     * Constructor for when the process never got an HTTP response
     *
     * @param _cause the Exception that caused the problem
     */
    public WebServiceError(Throwable _cause) {
        super();
        initCause(_cause);
    }

    /**
     * Return the best possible error from the information available
     *
     * @return error message
     */
    public String getMessage() {
        if (webResponse != null) {
            return webResponse.getError();
        }
        if (getCause() != null) {
            return getCause().getMessage();
        }
        return "Unknown error";
    }

    public WebResponse getWebResponse() {
        return webResponse;
    }
}
