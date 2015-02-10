package co.tapdatapp.tapandroid.remotedata;

import android.test.AndroidTestCase;

import co.tapdatapp.tapandroid.localdata.Transaction;

public class TransactionCodecTest extends AndroidTestCase {

    public void testUnmarshall() throws Exception {
        TransactionCodec tc = new TransactionCodec();
        for (String testValue : in) {
            Transaction t = tc.unmarshall(testValue);
        }
    }

    private final String[] in = {
        "{\"id\": \"id1\", \"date\": \"2012-04-17 17:43:00\", " +
        "\"payload_image\": \"http://www.example.com\", " +
        "\"payload_thumb\": \"http://www.example.com\", " +
        "\"amount\": 100, \"dollar_amount\": 100, " +
        "\"comment\": \"This is a test transaction\", " +
        "\"other_user_thumb\": \"http://www.example.com\", " +
        "\"other_user_nickname\": \"nickname\"}"
    };
}
