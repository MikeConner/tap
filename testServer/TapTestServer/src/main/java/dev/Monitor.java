
package dev;

public class Monitor {
  
  public static void trace(Throwable t) {
    trace(t.toString());
  }
  
  public static void trace(String message) {
    System.out.println(message);
  }
}
