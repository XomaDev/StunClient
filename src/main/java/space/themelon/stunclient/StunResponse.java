package space.themelon.stunclient;

import java.net.InetAddress;

public class StunResponse {

  public static final int TYPE_IPV4 = 1;
  public static final int TYPE_IPV6 = 2;

  public final int type;
  public final InetAddress address;
  public final int port;

  public StunResponse(int type, InetAddress address, int port) {
    this.type = type;
    this.address = address;
    this.port = port;
  }

  @Override
  public String toString() {
    return "StunResponse{" +
            "type=" + (type == TYPE_IPV4 ? "Ipv4" : "Ipv6") +
            ", address=" + address +
            ", port=" + port +
            '}';
  }
}
