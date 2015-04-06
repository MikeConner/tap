
package co.tapdatapp.taptestserver.controllers;

import co.tapdatapp.taptestserver.entities.PayloadObject;
import java.util.UUID;

public class NfcTag {

  public String authId;
  public int currencyId;
  public String name;
  public PayloadObject[] payloads;
  
  private String id;
  
  public void generateId() {
    id = UUID.randomUUID().toString().replace("-", "").substring(10, 20);
  }
  
  public void setId(String to) {
    if (to.length() != 10 || to.contains("-")) {
      throw new AssertionError("Invalid Tag ID: " + to);
    }
    id = to;
  }
  
  public String getId() {
    if (id.contains("-")) {
      throw new AssertionError("Invalid tag ID: " + id);
    }
    return id;
  }
  
  public void setIdFromFriendly(String to) {
    if (to.length() != 12) {
      throw new AssertionError("Invalid friendly Tag ID: " + to);
    }
    id = to.replace("-", "");
  }
  
  public String friendlyId() {
    return id.substring(0, 3) + "-" + id.substring(3, 6) + "-" + id.substring(6);
  }
  
}
