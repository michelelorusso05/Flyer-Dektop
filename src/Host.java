import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

public class Host {
    final private InetAddress ip;
    final private String name;
    private int port;
    final private int type;
    final private int packetType;

    public Host(InetAddress ip, String name, int port, int type, int packetType) {
        this.ip = ip;
        this.name = name;
        this.port = port;
        this.type = type;
        this.packetType = packetType;
    }

    public InetAddress getIp() {
        return ip;
    }

    public String getName() {
        return name;
    }

    public int getPort() {
        return port;
    }

    public int getType() {
        return type;
    }
    public void updatePort(int newPort) {
        port = newPort;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPacketType() {
        return packetType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != Host.class) return false;
        Host host = (Host) obj;
        return this.ip.equals(host.getIp());
    }

    public static String getHostname() {
        try {
            return Inet4Address.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    public static ArrayList<NetworkInterface> getActiveInterfaces() throws SocketException {
        ArrayList<NetworkInterface> interfaces = new ArrayList<>();

        for (Enumeration<NetworkInterface> it = NetworkInterface.getNetworkInterfaces(); it.hasMoreElements();) {
            NetworkInterface networkInterface = it.nextElement();

            boolean valid = false;
            if (networkInterface.isLoopback() || !networkInterface.isUp()) continue;
            for (InterfaceAddress addr : networkInterface.getInterfaceAddresses()) {
                if (addr.getBroadcast() == null) continue;
                if (addr.getAddress().isSiteLocalAddress()) valid = true;
            }

            if (valid) {
                interfaces.add(networkInterface);
            }
        }

        return interfaces;
    }
}