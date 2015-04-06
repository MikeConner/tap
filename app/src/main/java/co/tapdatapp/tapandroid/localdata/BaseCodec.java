/**
 * Code common to all Codec classes
 */

package co.tapdatapp.tapandroid.localdata;

public abstract class BaseCodec {

    /**
     * Return an alternate value if the specified value is null or
     * equals the string "null" (because JSON seems to generate a
     * string with the value "null" when a value is null)
     *
     * @param value The desired value
     * @param replace The value to use if "value" is null
     * @return per description
     */
    protected String ifNull(String value, String replace) {
        if (value == null || "null".equals(value)) {
            return replace;
        }
        else {
            return value;
        }
    }

}
