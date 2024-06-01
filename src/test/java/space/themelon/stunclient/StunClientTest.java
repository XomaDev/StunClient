package space.themelon.stunclient;

import java.io.IOException;
import java.net.InetAddress;

public class StunClientTest {
  public static void main(String[] args) throws IOException {
    StunClient client = new StunClient("stun.l.google.com", 19302);
    StunResponse response = client.request();

    InetAddress address = response.address;
    int port = response.port;
    boolean isIpv4 = response.type == StunResponse.TYPE_IPV4;

    System.out.println(address);
    System.out.println(port);
    System.out.println(isIpv4);
  }
}
