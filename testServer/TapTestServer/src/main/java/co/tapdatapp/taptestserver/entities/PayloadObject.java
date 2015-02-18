/**
 * I guess this is a Yapa?
 */
package co.tapdatapp.taptestserver.entities;

import java.util.UUID;

public class PayloadObject {
  
  public String uri;
  public String text;
  public int threshold;
  public String payload_image;
  public String payload_thumb;
  
  private int nfc_tag_id;
  private String slug;
  
  public void setTagId(int to) {
    nfc_tag_id = to;
  }
  
  public boolean slugEquals(String otherId) {
    return slug.equals(otherId);
  }
  
  public void generateSlug() {
    slug = UUID.randomUUID().toString().replace("-", "");
  }
  
  public String getSlug() {
    return slug;
  }
  
}
