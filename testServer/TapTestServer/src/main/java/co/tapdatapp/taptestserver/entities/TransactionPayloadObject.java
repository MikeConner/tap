/**
 * Payload objects are different depending on which part of the API is
 * sending them. This is the one from a new transaction request.
 */
package co.tapdatapp.taptestserver.entities;

public class TransactionPayloadObject {
  public String text;
  public String uri;
  public String image;
  public String thumb;
  
  public TransactionPayloadObject(PayloadObject p) {
    text = p.content;
    uri = p.uri;
    image = p.payload_image;
    thumb = p.payload_thumb;
  }
  
}
