/**
 * Don't store Tags in teh database a this time. TagList just keeps
 * them in memory, there shouldn't be very many.
 *
 * Probably no need to make this more complicated than and alias
 * for a HashMap. The key is the tagid/slug, and the value is actual
 * Tag object.
 */

package co.tapdatapp.tapandroid.tags;

import java.util.HashMap;

import co.tapdatapp.tapandroid.localdata.Tag;

public class TagList extends HashMap<String, Tag> {

}
