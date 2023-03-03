import java.net.DatagramPacket;

public class PacketUtils {
    public static byte[] encapsulate(int port, int deviceType, int packetType, String name) {
        byte[] packet = new byte[132];
        packet[0] = (byte) ((port >>> 8) & 255);
        packet[1] = (byte) (port & 255);
        packet[2] = (byte) deviceType;
        packet[3] = (byte) packetType;
        name = name.substring(0, Math.min(64, name.length()));
        System.arraycopy(name.getBytes(), 0, packet, 4, name.length());
        return packet;
    }
    public static Host deencapsulate(DatagramPacket packet) {
        byte[] data = packet.getData();
        String name = new String(data, 4, 128);
        int port = Byte.toUnsignedInt(data[1]) + (Byte.toUnsignedInt(data[0]) << 8);
        int deviceType = data[2];
        int packetType = data[3];
        return new Host(packet.getAddress(), name, port, deviceType, packetType);
    }
}
