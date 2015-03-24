/**
 * I guess this is a Yapa?
 */
package co.tapdatapp.taptestserver.entities;

import java.util.UUID;

public class PayloadObject {
  
  public String uri;
  public String content;
  public int threshold;
  public String payload_image;
  public String payload_thumb;
  public String description;
  public String content_type;
  
  private String nfc_tag_id;
  private String slug;
  
  public void setTagId(String to) {
    nfc_tag_id = to;
  }
  
  public String getTagId() {
    return nfc_tag_id;
  }
  
  public boolean tagIdEquals(String otherId) {
    otherId = otherId.replace("-", "");
    return nfc_tag_id.equals(otherId);
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
