/**
 * Carrier class to encapsulate exceptions thrown by the web service
 */

package co.tapdatapp.tapandroid.remotedata;

public class WebServiceError extends Exception {

    private WebResponse webResponse = null;

    private Throwable cause = null;

    /**
     * Constructor for when a response was actually received, but was
     * in error for some reason.
     *
     * @param wr Received response
     */
    public WebServiceError(WebResponse wr) {
        super();
        webResponse = wr;
    }

    /**
     * Constructor for when the process never got an HTTP response
     *
     * @param _cause the Exception that caused the problem
     */
    public WebServiceError(Throwable _cause) {
        super();
        cause = _cause;
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
        if (cause != null) {
            return cause.getMessage();
        }
        return "Unknown error";
    }
}
