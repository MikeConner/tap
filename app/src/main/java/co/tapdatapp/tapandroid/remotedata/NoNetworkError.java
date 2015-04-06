package co.tapdatapp.tapandroid.remotedata;

public class NoNetworkError extends RuntimeException {

    public NoNetworkError() {
        super();
    }

    public NoNetworkError(String message) {
        super(message);
    }

}
