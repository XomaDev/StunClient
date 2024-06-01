<h1 align="center">Simple Stun Client</h1>
<p align="center">Standard Stun client (RFC 5389) implementation in Java</p>

## Usage

Create a reusable `StunClient` object

```java
StunClient client = new StunClient("stun.l.google.com", 19302);
                     // or new StunClient(stunInetAddress, port);
```

Execute a request to the stun server

```java
StunResponse response = client.request();

// StunResponse{type=Ipv4, address=/...51.1..7.225, port=13474}

InetAddress address = response.address;
int port = response.port;
boolean isIpv4 = response.type == StunResponse.TYPE_IPV4;
```


