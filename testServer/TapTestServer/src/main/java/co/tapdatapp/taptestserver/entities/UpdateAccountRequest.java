/**
 * Request object to modify account details
 */
package co.tapdatapp.taptestserver.entities;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class UpdateAccountRequest {
  public UserObject user;

  public class UserObject {
    public String name = null;
    public String email = null;
    public String mobile_profile_thumb_url = null;
  }
 
  /**
   * Having difficulty convincing Jersey to automatically build this object,
   * so just brute-force it.
   * 
   * If any of the items are not found in the request, just leave them null
   * 
   * @param json the raw JSON
   */
  public UpdateAccountRequest(String json) throws JSONException {
    JSONObject jo = new JSONObject(json).getJSONObject("user");
    user = new UserObject();
    try {
      user.name = jo.getString("name");
    }
    catch (JSONException je) {
      if (!je.getMessage().contains("not found")) {
        throw je;
      }
    }
    try {
      user.email = jo.getString("email");
    }
    catch (JSONException je) {
      if (!je.getMessage().contains("not found")) {
        throw je;
      }
    }
    try {
      user.mobile_profile_thumb_url = jo.getString("mobile_profile_thumb_url");
    }
    catch (JSONException je) {
      if (!je.getMessage().contains("not found")) {
        throw je;
      }
    }
  }
  
}
