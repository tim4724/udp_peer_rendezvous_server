import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * A Peer sends a packet to the server with the following info:
 * - his own id
 * - the id of the other peer he wants to connect to
 * - his local ip and port
 * <p>
 * The Server looks wether he has already received a packet from the other peer
 * and responds with the public ip and port and the local ip and port of the other peer.
 * If the other peer has not connected yet, the server will not respond.
 */
public class RendezVousServer {
    private static Map<Integer, AddressHolder> knownPeerAddresses;

    public static void main(String args[]) throws SocketException {
        knownPeerAddresses = new HashMap<>();
        CleanUpThread cleanUpThread = new CleanUpThread();
        cleanUpThread.start();
        byte[] buffer = new byte[Payload.totalLen];
        DatagramSocket socket = new DatagramSocket(3845);
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

        while (true) {
            try {
                socket.receive(packet);

                //parse packet
                int senderId = Payload.getSenderId(packet.getData());
                int otherId = Payload.getOtherId(packet.getData());
                AddressHolder addresses = parsePacketForAddresses(packet);
                System.out.println("Received packet from " + senderId + ": " + addressToString(addresses.publicAddress));

                //cache the peer id and the addresses
                knownPeerAddresses.put(senderId, addresses);

                //respond with the addresses of the other peer if available
                AddressHolder desiredAddresses = getAddresses(otherId);
                if (desiredAddresses != null) {
                    buffer = Payload.build(buffer, senderId, otherId, 2, desiredAddresses.localAddress, desiredAddresses.publicAddress);
                    packet.setData(buffer);
                    socket.send(packet);
                    System.out.println("Respond with addresses of " + otherId + ": " + addressToString(desiredAddresses.localAddress) + " " + addressToString(desiredAddresses.publicAddress));
                } else {
                    //Other peer has not connected yet, do send anything in order to trigger timeout if other peer does not connect in time
                    System.out.println("The other peer " + otherId + " has not connected to the server yet");
                }

                if (knownPeerAddresses.size() > 10000) {
                    cleanUpThread.interrupt();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static AddressHolder parsePacketForAddresses(DatagramPacket packet) throws UnknownHostException {
        InetSocketAddress publicAddress = (InetSocketAddress) packet.getSocketAddress();
        InetSocketAddress localAddress = Payload.parseLocalAddress(packet.getData());
        return new AddressHolder(publicAddress, localAddress);
    }

    /**
     * Find the addresses of a certain peer and check wether these are still live.
     *
     * @param peerId the id of the peer we are interested in.
     * @return AddressHolder with the addresses or null
     */
    private static AddressHolder getAddresses(int peerId) {
        AddressHolder addresses = knownPeerAddresses.get(peerId);
        if (addresses != null && addresses.isLive()) {
            return addresses;
        }
        return null;
    }

    /**
     * class to manage the two addresses every peer has.
     * The addresses are only valid for a certain time (TTL)
     */
    private static class AddressHolder {
        static final int TTL = 30000; //time to live 30 seconds
        final InetSocketAddress publicAddress, localAddress;
        final long createdAt;

        AddressHolder(InetSocketAddress publicAddress, InetSocketAddress localAddress) {
            this.publicAddress = publicAddress;
            this.localAddress = localAddress;
            createdAt = System.currentTimeMillis();
        }

        boolean isLive() {
            return System.currentTimeMillis() < (createdAt + TTL);
        }
    }

    private static String addressToString(InetSocketAddress address) {
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }

    /**
     * Clean up the knwonPeerAddresses map every hour
     */
    static class CleanUpThread extends Thread {
        @Override
        public void run() {
            //remove all addressholders that are not live anymore
            knownPeerAddresses.entrySet().removeIf(entry -> entry.getValue().isLive());
            try {
                sleep(3600000);//1 hour
            } catch (InterruptedException ignore) {
            }
        }
    }
}