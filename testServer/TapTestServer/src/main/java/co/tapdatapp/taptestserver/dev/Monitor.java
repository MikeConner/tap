
package co.tapdatapp.taptestserver.dev;

import co.tapdatapp.taptestserver.dev.entities.ServerMessage;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Monitor {
  
  private static final ConcurrentLinkedDeque<ServerMessage> messages;
  private static final int MAX_MESSAGES = 20;
  
  static {
    messages = new ConcurrentLinkedDeque<>();
  }
  
  public static void trace(Throwable t) {
    trace(t.toString());
  }
  
  public static void trace(String message) {
    System.out.println(message);
    ServerMessage sm = new ServerMessage(new Date(), message);
    messages.addLast(sm);
    pruneMessages();
  }
  
  public static ServerMessage[] getMessages() {
    return messages.toArray(new ServerMessage[messages.size()]);
  }
  
  private static void pruneMessages() {
    while (messages.size() > MAX_MESSAGES) {
      messages.removeFirst();
    }
  }
}
