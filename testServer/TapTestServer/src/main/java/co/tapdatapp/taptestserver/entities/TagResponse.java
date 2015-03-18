
package co.tapdatapp.taptestserver.entities;

import co.tapdatapp.taptestserver.controllers.NfcTag;

public class TagResponse {
  public int system_id;
  public String id;
  public String name;
  public final PayloadObject[] payloads;
  
  public TagResponse(NfcTag t) {
    payloads = t.payloads;
    system_id = 0;
    id = t.friendlyId();
    name = t.name;
  }
  
}
