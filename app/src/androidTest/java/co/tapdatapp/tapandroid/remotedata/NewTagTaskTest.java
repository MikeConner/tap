package co.tapdatapp.tapandroid.remotedata;

import co.tapdatapp.tapandroid.BaseUnitTest;

public class NewTagTaskTest extends BaseUnitTest {

    public void testNewTagRequest() throws Exception {
        NewTagTask ntt = new NewTagTask();
        TagCodec tag = ntt.getTagFromServer();
        assertNotNull("Null ID returned", tag.getId());
        assertTrue("Empty id", !tag.getId().isEmpty());
        assertNotNull("Null name returned", tag.getName());
        assertTrue("empty name", !tag.getName().isEmpty());
    }

}
