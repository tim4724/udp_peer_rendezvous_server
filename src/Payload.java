import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * build a byte array with all necessary information.
 * read info from byte array
 */
class Payload {
    static final int totalLen = 28;

    private static final int senderIdIndex = 0;         //4 Bytes
    private static final int receiverIdIndex = 4;       //4 Bytes
    private static final int stateIndex = 8;            //4 Bytes
    private static final int localAddressIndex = 12;    //4 Bytes
    private static final int localPortIndex = 16;       //4 Bytes
    private static final int publicAddressIndex = 20;   //4 Bytes
    private static final int publicPortIndex = 24;      //4 Bytes

    static byte[] build(byte[] buffer, int senderId, int receiverId, int state, InetSocketAddress localAddress, InetSocketAddress publicAddress) {
        ByteStuff.putInt(senderId, buffer, senderIdIndex);
        ByteStuff.putInt(receiverId, buffer, receiverIdIndex);
        ByteStuff.putInt(state, buffer, stateIndex);
        ByteStuff.putBytes(localAddress.getAddress().getAddress(), buffer, localAddressIndex);
        ByteStuff.putInt(localAddress.getPort(), buffer, localPortIndex);
        ByteStuff.putBytes(publicAddress.getAddress().getAddress(), buffer, publicAddressIndex);
        ByteStuff.putInt(publicAddress.getPort(), buffer, publicPortIndex);
        return buffer;
    }

    static InetSocketAddress parseLocalAddress(byte[] data) throws UnknownHostException {
        byte[] localIp = ByteStuff.readBytes(data, localAddressIndex, localAddressIndex + 4);
        int localPort = ByteStuff.readInt(data, localPortIndex);
        InetAddress localInetAddress = InetAddress.getByAddress(localIp);
        return new InetSocketAddress(localInetAddress, localPort);
    }

    static int getSenderId(byte[] data) {
        return ByteStuff.readInt(data, senderIdIndex);
    }

    static int getOtherId(byte[] data) {
        return ByteStuff.readInt(data, receiverIdIndex);
    }

    static int getState(byte[] data) {
        return ByteStuff.readInt(data, stateIndex);
    }
}
