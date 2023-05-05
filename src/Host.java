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
    /**
     * Returns the ip of the host
     * @return InetAddress ip
     * */
    public InetAddress getIp() {
        return ip;
    }
    /**
     * Returns the name of the host
     * @return String name
     * */
    public String getName() {
        return name;
    }
    /**
     * Returns the TCP port of the host
     * @return int port
     * */
    public int getPort() {
        return port;
    }
    /**
     * Returns the device type of the host
     * @return int type
     * */
    public int getType() {
        return type;
    }
    /**
     * Updates the TCP port of the host
     * @param newPort new port
     * */
    public void updatePort(int newPort) {
        port = newPort;
    }
    /**
     * Returns the type of packet that the host receives/sends
     * @return int packetType
     * */
    public int getPacketType() {
        return packetType;
    }
    /**
     * Compares two Host
     * @param obj the Host to compare
     * @return boolean isEqual
     * */
    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != Host.class) return false;
        Host host = (Host) obj;
        return this.ip.equals(host.getIp());
    }
    /**
     * Returns the name of the host
     * @return String hostName
     * */
    public static String getHostname() {
        try {
            return Inet4Address.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }
    /**
     * Returns all active interfaces of the device
     * @return ArrayList of NetworkInterface activeInterfaces
     * */
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