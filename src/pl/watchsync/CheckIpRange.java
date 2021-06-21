package pl.watchsync;

import java.net.Inet4Address;
import java.net.InetAddress;


//This class checks IP range for security reasons
public class CheckIpRange {

    //converts string IP to integer
    private static int convertIpToInteger(final String ip) {
        try {
            String s = ip;
            Inet4Address a = (Inet4Address) InetAddress.getByName(s);
            byte[] b = a.getAddress();
            int i = ((b[0] & 0xFF) << 24) | ((b[1] & 0xFF) << 16) | ((b[2] & 0xFF) << 8) | ((b[3] & 0xFF) << 0);
            return i;
        } catch (final Throwable ignored) {
            return -1;
        }
    }

    //Allows to check range in CIDR notation
    public static boolean checkIpInSubnet(String ip, String mask, String ipToCheck) {
        int integerIp = convertIpToInteger(ip);
        int integerIpToCheck = convertIpToInteger(ipToCheck);
        int bits = Integer.parseInt(mask);
        int byte_mask = -1 << (32 - bits);
        return (integerIp & byte_mask) == (integerIpToCheck & byte_mask);
    }
}
