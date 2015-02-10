package co.tapdatapp.tapandroid.remotedata;

import org.json.JSONObject;

import java.util.List;

import co.tapdatapp.tapandroid.BaseUnitTest;
import co.tapdatapp.tapandroid.localdata.Denomination;

public class CurrencyCodecTest extends BaseUnitTest {

    /**
     * Just put variations on valid JSON through this to make sure
     * they don't throw an exception.
     *
     * @throws Exception
     */
    public void testParseJSON() throws Exception {
        CurrencyCodec cc = new CurrencyCodec();
        for (String testValue : in) {
            cc.parse(1, new JSONObject(testValue));
        }
    }

    /**
     * Use element 0 in the test array to check that fields are properly
     * filled in.
     *
     * Also tests aspects of Denomination
     *
     * @throws Exception
     */
    public void testFieldsCorrect() throws Exception {
        CurrencyCodec cc = new CurrencyCodec();
        cc.parse(1, new JSONObject(in[0]));
        assertEquals("Fun Bucks", cc.getName());
        assertEquals("http://www.tapdatapp.co/mobile/v1/icons?id=134", cc.getIcon());
        assertEquals("$", cc.getSymbol());
        List<Denomination> dl = cc.getDenominations();
        Denomination d = dl.get(0);
        assertEquals("http://www.tapdatapp.co/mobile/v1/icons?id=634", d.getURL());
        assertEquals(1, d.getAmount());
        d = dl.get(1);
        assertEquals("http://www.tapdatapp.co/mobile/v1/icons?id=174", d.getURL());
        assertEquals(2, d.getAmount());
        d = dl.get(2);
        assertEquals("http://www.tapdatapp.co/mobile/v1/icons?id=124", d.getURL());
        assertEquals(5, d.getAmount());
    }

    private final String[] in = {
        "{\"name\" : \"Fun Bucks\",\n" +
            "\"icon\" : \"http://www.tapdatapp.co/mobile/v1/icons?id=134\",\n" +
            "\"status\" : \"active\",\n" +
            "\"icon_processing\" : true,\n" +
            "\"symbol\" : \"$\",\n" +
            "\"denominations\" : [\n" +
            " {\"amount\": 1,\n" +
            "  \"icon\": \"http://www.tapdatapp.co/mobile/v1/icons?id=634\"},\n" +
            " {\"amount\": 2,\n" +
            "  \"icon\": \"http://www.tapdatapp.co/mobile/v1/icons?id=174\"},\n" +
            " {\"amount\": 5,\n" +
            "  \"icon\": \"http://www.tapdatapp.co/mobile/v1/icons?id=124\"}\n" +
            "]\n}",
        "{\"name\" : \"Mrh\",\n" +
            "\"icon\" : \"http://www.tapdatapp.co/mobile/v1/icons?id=134\",\n" +
            "\"status\" : \"active\",\n" +
            "\"icon_processing\" : false,\n" +
            "\"symbol\" : \"%\",\n" +
            "\"denominations\" : [\n" +
            "]\n}"
    };

}
