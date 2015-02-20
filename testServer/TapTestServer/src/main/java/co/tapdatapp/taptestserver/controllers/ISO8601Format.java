/**
 * Just to make it easy to get a reliable ISO8601 date formatter
 */
package co.tapdatapp.taptestserver.controllers;

import java.text.SimpleDateFormat;

public class ISO8601Format extends SimpleDateFormat {
  
  public ISO8601Format() {
    super("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
  }
  
}
