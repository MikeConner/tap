/**
 * Some endpoints return a {response : ... } pattern with only a single child
 * object.
 */
package co.tapdatapp.taptestserver.entities;

public class ResponseResponse {
  public Object response;
  
  public ResponseResponse(Object o) {
    response = o;
  }
}
