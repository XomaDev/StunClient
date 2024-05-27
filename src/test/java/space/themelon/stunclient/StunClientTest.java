package space.themelon.stunclient;

import java.io.IOException;

public class StunClientTest {
  public static void main(String[] args) throws IOException {
    StunClient client = new StunClient("stun.l.google.com", 19302);
    System.out.println(client.request());
  }
}
