/**
 * Request object to modify account details
 */
package co.tapdatapp.taptestserver.entities;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class UpdateAccountRequest {
  public UserObject user;

  public class UserObject {
    public String name;
    public String email;
    public String mobile_profile_thumb_url;
  }
 
  /**
   * Having difficulty convincing Jersey to automatically build this object,
   * so just brute-force it.
   * 
   * @param json the raw JSON
   */
  public UpdateAccountRequest(String json) throws JSONException {
    JSONObject jo = new JSONObject(json).getJSONObject("user");
    user = new UserObject();
    user.name = jo.getString("name");
    user.email = jo.getString("email");
    user.mobile_profile_thumb_url = jo.getString("mobile_profile_thumb_url");
  }
  
}
