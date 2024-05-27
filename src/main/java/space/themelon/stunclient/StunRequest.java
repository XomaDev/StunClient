package space.themelon.stunclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class StunRequest {

  private static final int MAGIC_COOKIE = 0x2112A442;

  public static void request(String server, int port) throws IOException {
    byte[] request = new byte[20];
    // [ [00 bits reserved] [Message Type 14 bits] [Message Length 16 bits (2^4)] [Magic Cookie 32 bits Int64] [12 rand bytes (12^8 bits) ] ]
    // request[0] = 0;
    request[1] = 1;
    // request[2] = 0;
    // request[3] = 0;
    int magicCookie = 0x2112A442;
    request[4] = (byte) (magicCookie >> 24);
    request[5] = (byte) (magicCookie >> 16);
    request[6] = (byte) (magicCookie >> 8);
    request[7] = (byte) (magicCookie);

    Random random = new Random();
    for (int i = 8; i < 20; i++) {
      request[i] = (byte) random.nextInt();
    }

    DatagramSocket socket = new DatagramSocket();
    socket.setSoTimeout(3 * 1000);
    socket.send(new DatagramPacket(request, 20, InetAddress.getByName(server), port));

    byte[] response = new byte[1024];
    DatagramPacket responsePacket = new DatagramPacket(response, response.length);
    socket.receive(responsePacket);

    BitInputStream bitsInput = new BitInputStream(
            new ByteArrayInputStream(response, 0, responsePacket.getLength()));
    bitsInput.skip(2 + 2 + 4 + 12);
    // Message Type [2 bytes] + Message Length [2 bytes] + Magic Cookie [4 bytes] + Transaction Id [12 bytes]

    while (bitsInput.available() > 0) {
      int type = bitsInput.readShort16() & 0xff;
      int attributeSize = bitsInput.readShort16() & 0xffff;

      if (type != 0x0020) {
        // not a XOR-MAPPED-ADDRESS
        bitsInput.skip(attributeSize);
      } else {
        bitsInput.read();
        int family = bitsInput.read();

        // [Ignore First Byte + Family Byte]
        int publicPort = ((bitsInput.read() & 0xff) << 8 | bitsInput.read() & 0xff) ^ (MAGIC_COOKIE >> 16);
        byte[] publicIpBytes = new byte[4];
        bitsInput.read(publicIpBytes);

        for (int i = 0; i < 4; i++) {
          publicIpBytes[i] ^= (byte) (MAGIC_COOKIE >> (24 - i * 8));
        }
        InetAddress publicIp = InetAddress.getByAddress(publicIpBytes);

        System.out.println("Family: " + (family == 1 ? "Ipv4" : "Ipv6"));
        System.out.println("Public Port: " + publicPort);
        System.out.println("Public Ip: " + publicIp);
        break;
      }
    }
  }

  public static void main(String[] args) throws IOException {
    request("stun.l.google.com", 19302);
  }
}
