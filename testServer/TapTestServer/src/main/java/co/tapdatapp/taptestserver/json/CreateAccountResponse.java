/**
 * Response to new account creation
 * 
 * { "response" : { "auth_token" : "some string", "nickname" : "some string"} }
 * 
 */
package co.tapdatapp.taptestserver.json;

public class CreateAccountResponse {
  public final Response response = new Response();
  
  public class Response {
    public String auth_token;
    public String nickname;
  }
  
}
