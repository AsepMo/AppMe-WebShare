package com.appme.story.engine.app.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.Inet4Address;
import java.util.Collections;
import java.util.List;
import java.util.Enumeration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Pattern;

import org.apache.http.conn.util.InetAddressUtils;

public class NetWorkUtils {
	
	/**
     * Ipv4 address check.
     */
    private static final Pattern IPV4_PATTERN = Pattern.compile("^(" +
                                                                "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}" +
                                                                "([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])$");


    public static String getLocalIpAddress() {
        try {
            String ipv4;

            ArrayList<NetworkInterface> mylist = Collections
                .list(NetworkInterface.getNetworkInterfaces());

            for (NetworkInterface ni : mylist) {

                ArrayList<InetAddress> ialist = Collections.list(ni
                                                                 .getInetAddresses());
                for (InetAddress address : ialist) {
                    if (!address.isLoopbackAddress()
                        && InetAddressUtils.isIPv4Address(ipv4 = address
                                                          .getHostAddress())) {
                        return ipv4;
                    }
                }

            }

        } catch (SocketException ex) {

        }
        return null;
	} 
    
    private static String getIPAddress(Acceptable acceptable) {
        String ips = null;
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (acceptable.isAcceptable(intf)) {
                    List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                    for (InetAddress addr : addrs) {
                        if (addr.getClass().equals(Inet4Address.class)) {
                            ips = addr.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ips;
    }

    public static String getLocalHostIpAddress() {
        String ip = getIPAddress(new Acceptable() {
                @Override
                public boolean isAcceptable(NetworkInterface networkInterface) {
                    String name = networkInterface.getName();
                    return name.startsWith("lo");
                }
            });
        return ip == null ?  "localhost" : ip;
    }

    public static String getWifiIpAddress() {
        return getIPAddress(new Acceptable() {
                @Override
                public boolean isAcceptable(NetworkInterface networkInterface) {
                    String name = networkInterface.getName();
                    return name.startsWith("wl") || name.startsWith("en");
                }
            });
    }


    public static String getHotspotIpAddress() {
        return getIPAddress(new Acceptable() {
                @Override
                public boolean isAcceptable(NetworkInterface networkInterface) {
                    String name = networkInterface.getName();
                    return name.startsWith("wl") || name.startsWith("ap");
                }
            });
    }

    public interface Acceptable {
        boolean isAcceptable(NetworkInterface networkInterface);
    }
    
    /**
     * Check if valid IPV4 address.
     *
     * @param input the address string to check for validity.
     * @return True if the input parameter is a valid IPv4 address.
     */
    public static boolean isIPv4Address(String input) {
        return IPV4_PATTERN.matcher(input).matches();
    }

    /**
     * Get local Ip address.
     */
    public static InetAddress getLocalIPAddress() {
        Enumeration<NetworkInterface> enumeration = null;
        try {
            enumeration = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            e.printStackTrace();
        }
        if (enumeration != null) {
            while (enumeration.hasMoreElements()) {
                NetworkInterface nif = enumeration.nextElement();
                Enumeration<InetAddress> inetAddresses = nif.getInetAddresses();
                if (inetAddresses != null)
                    while (inetAddresses.hasMoreElements()) {
                        InetAddress inetAddress = inetAddresses.nextElement();
                        if (!inetAddress.isLoopbackAddress() && isIPv4Address(inetAddress.getHostAddress())) {
                            return inetAddress;
                        }
                    }
            }
        }
        return null;
    }
	
    public static boolean getNetworkStatus(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = manager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }
    
    public static boolean isConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }
}
