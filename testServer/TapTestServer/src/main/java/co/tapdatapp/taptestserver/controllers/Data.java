/**
 * store "files" for later use
 */
package co.tapdatapp.taptestserver.controllers;

import co.tapdatapp.taptestserver.entities.StoreResponse;
import java.util.HashMap;
import java.util.UUID;

public class Data {
  
  private final HashMap<String, byte[]> data;
  
  public Data() {
    data = new HashMap<>();
  }
  
  public StoreResponse storeNew(byte[] in) {
    StoreResponse rv = new StoreResponse();
    rv.id = UUID.randomUUID().toString();
    data.put(rv.id, in);
    return rv;
  }
  
  public StoreResponse update(String id, byte[] in) {
    StoreResponse rv = new StoreResponse();
    rv.id = id;
    data.put(rv.id, in);
    return rv;
  }
  
  public byte[] fetch(String id) {
    return data.get(id);
  }
  
}
