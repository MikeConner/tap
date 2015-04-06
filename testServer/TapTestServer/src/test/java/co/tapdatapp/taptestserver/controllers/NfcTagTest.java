
package co.tapdatapp.taptestserver.controllers;

import org.junit.Test;
import static org.junit.Assert.*;

public class NfcTagTest {

  @Test
  public void testFriendyId0() {
    final String in = "1234567890";
    final String out = "123-456-7890";
    NfcTag tag = new NfcTag();
    tag.setId(in);
    assertEquals(out, tag.friendlyId());
  }
  
  @Test
  public void testFriendyId1() {
    final String in = "1234567890";
    final String out = "123-456-7890";
    NfcTag tag = new NfcTag();
    tag.setIdFromFriendly(out);
    assertEquals(in, tag.getId());
  }
  
}
