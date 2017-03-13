import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

/**
 * Rendezvous Server to establish peer to peer connections
 * <p>
 * A Peer registeres by sending his id and his local address to this Server. Local address and public address will then be cached.
 * Next a peer will request the addresses of another peer. If the server has these cached, he will respond with the addresses.
 */
public class RendezVousServer {

    public static void main(String args[]) throws SocketException {
        byte[] data = new byte[Payload.totalLen];
        DatagramSocket socket = new DatagramSocket(3845);
        DatagramPacket packet = new DatagramPacket(data, data.length);

        while (true) {
            try {
                socket.receive(packet);

                //parse
                int senderId = Payload.getSenderId(data);
                int otherId = Payload.getOtherId(packet.getData());
                int state = Payload.getState(data);

                switch (state) {
                    case 1://register peer
                        InetSocketAddress localAddress = Payload.parseLocalAddress(packet.getData());
                        InetSocketAddress publicAddress = (InetSocketAddress) packet.getSocketAddress();
                        //cache the peer id and the addresses
                        System.out.println(senderId + " registers for " + otherId + " " + addressToString(publicAddress) + ' ' + addressToString(localAddress));
                        AddressStorage.register(senderId, otherId, publicAddress, localAddress);
                        //confirm registration (just send the packet back)
                        socket.send(packet);
                        break;

                    case 2://request addresses
                        //respond with the addresses of the other peer if available
                        AddressStorage.AddressHolder peerAddresses = AddressStorage.getAddresses(senderId, otherId);
                        if (peerAddresses != null) {
                            Payload.build(data, senderId, otherId, state, peerAddresses.localAddress, peerAddresses.publicAddress);
                            socket.send(packet);
                            System.out.println(senderId + " requested addresses of " + otherId);
                        }
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String addressToString(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }
}