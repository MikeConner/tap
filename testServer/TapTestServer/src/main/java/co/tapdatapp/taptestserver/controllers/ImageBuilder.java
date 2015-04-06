/**
 * Handle the on-the-fly generation of images
 */
package co.tapdatapp.taptestserver.controllers;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageBuilder {
  
  public static String getURL(int width, int height, String text) {
    // @TODO make this adjust the IP based on the server
    final String BASE = "https://www.skilive.net/tap/mobile/1/developer/image/";
    //final String BASE = "http://172.17.10.2:8080/TapTestServer/mobile/1/developer/image/";
    return BASE +
      width + "_" + height + "_" + text.replace(" ", "-").replace("_", "-");
  }
  
  public static byte[] getImage(String name) throws IOException {
    String[] bits = name.split("_");
    return getImage(Integer.parseInt(bits[0]), Integer.parseInt(bits[1]), bits[2]);
  }
  
  public static byte[]
  getImage(int width, int height, String text) throws IOException {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2 = image.createGraphics();
    g2.setBackground(Color.yellow);
    g2.drawChars(text.toCharArray(), 0, text.length(), 5, height - 5);
    ByteArrayOutputStream baos=new ByteArrayOutputStream();
    ImageIO.write(image, "png", baos);
    return baos.toByteArray();
  }
  
}
