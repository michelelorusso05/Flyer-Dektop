import java.net.DatagramPacket;

public class PacketUtils {
    public final static int discoveryVersion = 1;
    public final static int flowVersion = 1;
    public static byte[] encapsulate(int port, int deviceType, int packetType, String name) {
        byte[] packet = new byte[128];
        packet[0] = flowVersion;
        packet[1] = (byte) ((port >>> 8) & 255);
        packet[2] = (byte) (port & 255);
        packet[3] = (byte) deviceType;
        packet[4] = (byte) packetType;
        name = name.substring(0, Math.min(119, name.length()));
        System.arraycopy(name.getBytes(), 0, packet, 8, name.length());
        return packet;
    }
    public static Host deencapsulate(DatagramPacket packet) {
        byte[] data = packet.getData();
        String name = new String(data, 8, 120);
        int version = data[0];
        int port = Byte.toUnsignedInt(data[2]) + (Byte.toUnsignedInt(data[1]) << 8);
        int deviceType = data[3];
        int packetType = data[4];
        return new Host(packet.getAddress(), name, port, deviceType, packetType);
    }
}
