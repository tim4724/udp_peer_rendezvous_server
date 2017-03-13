import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Store addresses of peers.
 */
class AddressStorage {
    //Key is "<peer>:<OtherPeer>" (e.g. "16:23") the addresses are from peer 16, peer 23 is allowed to access the data
    private static Map<String, AddressHolder> knownPeerAddresses = new HashMap<>();

    /**
     * Cache a peers addresses. Only grant a certain "otherPeer" to access these addresses.
     *
     * @param ownId         the id of the peer
     * @param otherId       the peer who is allowed to access the data
     * @param localAddress  the local address of the peer
     * @param publicAddress the public address of the peer
     * @throws UnknownHostException
     */
    static void register(int ownId, int otherId, InetSocketAddress localAddress, InetSocketAddress publicAddress) throws UnknownHostException {
        AddressHolder addressHolder = new AddressHolder(localAddress, publicAddress);
        knownPeerAddresses.put(ownId + ":" + otherId, addressHolder);

        if (knownPeerAddresses.size() > 1000) {
            cleanUp();
        }
    }

    /**
     * Find the addresses of a certain peer and check wether these are still live.
     *
     * @param ownId  the id of the peer who requested the addresses
     * @param peerId the id of the peer we are interested in.
     * @return AddressHolder with the addresses or null
     */
    static AddressHolder getAddresses(int ownId, int peerId) {
        AddressHolder addresses = knownPeerAddresses.get(peerId + ":" + ownId);
        if (addresses != null && addresses.isLive()) {
            return addresses;
        }
        return null;
    }

    /**
     * remove all Map Entrys where the Addressholder is not live anymore
     */
    private static void cleanUp() {
        new Thread(() -> knownPeerAddresses.entrySet().removeIf(entry -> entry.getValue().isLive())).start();
    }

    /**
     * class to manage the two addresses every peer has.
     * The addresses are only valid for a certain time (TTL)
     */
    static class AddressHolder {
        static final int TTL = 30000; //time to live 30 seconds
        final InetSocketAddress localAddress, publicAddress;
        final long createdAt;

        private AddressHolder(InetSocketAddress localAddress, InetSocketAddress publicAddress) {
            this.localAddress = localAddress;
            this.publicAddress = publicAddress;
            createdAt = System.currentTimeMillis();
        }

        boolean isLive() {
            return System.currentTimeMillis() < (createdAt + TTL);
        }
    }
}
