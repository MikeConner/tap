
package co.tapdatapp.taptestserver.entities;

public class UserDetailsResponse {
  public String nickname;
  public String email;
  public String inbound_btc_address;
  public final InboundBtcQrCode inbound_btc_qrcode;
  public String outbound_btc_address;
  public int satoshi_balance;
  public String profile_image;
  public String profile_thumb;
  
  public UserDetailsResponse() {
    inbound_btc_qrcode = new InboundBtcQrCode();
  }
  
  public class InboundBtcQrCode {
    public final Url inbound_btc_qrcode;
    
    public InboundBtcQrCode() {
      inbound_btc_qrcode = new Url();
    }
  }
 
  public class Url {
    public String url;
  }
  
}
