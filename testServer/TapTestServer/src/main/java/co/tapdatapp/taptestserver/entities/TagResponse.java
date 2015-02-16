
package co.tapdatapp.taptestserver.entities;

import java.util.Random;
import java.util.UUID;

public class TagResponse {
  public final String tag_id;
  public final int id;
  public String name;
  
  private final String authId;
  
  /**
   * Creates a random one
   */
  public TagResponse(String auth) {
    authId = auth;
    tag_id = UUID.randomUUID().toString().replace("-", "").substring(5, 10);
    name = "Test tag";
    id = new Random().nextInt(999999);
  }
  
  public boolean belongsTo(String auth) {
    return authId.equals(auth);
  }
}
