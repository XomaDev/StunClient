package space.themelon.stunclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Random;

public class StunClient {

  private static final int MAGIC_COOKIE = 0x2112A442;

  private final InetAddress stunAddress;
  private final int port;

  public StunClient(String server, int port) throws UnknownHostException {
    stunAddress = InetAddress.getByName(server);
    this.port = port;
  }

  public StunClient(InetAddress stunAddress, int port) {
    this.stunAddress = stunAddress;
    this.port = port;
  }

  public StunResponse request() throws IOException {
    byte[] request = new byte[20];
    // [ [00 bits reserved] [Message Type 14 bits] [Message Length 16 bits (2^4)] [Magic Cookie 32 bits Int64] [12 rand bytes (12^8 bits) ] ]
    // request[0] = 0;
    request[1] = 1;
    // request[2] = 0;
    // request[3] = 0;
    request[4] = (byte) (MAGIC_COOKIE >> 24);
    request[5] = (byte) (MAGIC_COOKIE >> 16);
    request[6] = (byte) (MAGIC_COOKIE >> 8);
    request[7] = (byte) (MAGIC_COOKIE);

    Random random = new Random();
    for (int i = 8; i < 20; i++) {
      request[i] = (byte) random.nextInt();
    }

    DatagramSocket socket = new DatagramSocket();
    socket.setSoTimeout(3 * 1000);
    socket.send(new DatagramPacket(request, 20, stunAddress, port));

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
        continue;
      }
      bitsInput.read();
      int family = bitsInput.read();

      // [Ignore First Byte + Family Byte]
      int publicPort = ((bitsInput.read() & 0xff) << 8 | bitsInput.read() & 0xff) ^ (MAGIC_COOKIE >> 16);
      byte[] addrBytes = new byte[4];
      bitsInput.read(addrBytes);

      for (int i = 0; i < 4; i++) {
        addrBytes[i] ^= (byte) (MAGIC_COOKIE >> (24 - i * 8));
      }
      socket.close();
      return new StunResponse(family, InetAddress.getByAddress(addrBytes), publicPort);
    }
    socket.close();
    return null;
  }
}
