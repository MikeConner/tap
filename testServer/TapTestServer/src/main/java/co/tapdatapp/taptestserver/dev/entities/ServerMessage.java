
package co.tapdatapp.taptestserver.dev.entities;

import java.util.Date;

public class ServerMessage {
  public String timestamp;
  public String message;
  
  public ServerMessage(Date d, String m) {
    timestamp = d.toString();
    message = m;
  }
}
