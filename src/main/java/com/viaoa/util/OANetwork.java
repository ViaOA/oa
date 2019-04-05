package com.viaoa.util;

import java.net.InetAddress;

public class OANetwork {

    public static void findAllServers() throws Exception {
        InetAddress localhost = InetAddress.getLocalHost();

        byte[] ip = localhost.getAddress();
        
        for (int i = 210; i <= 254; i++) {
            ip[3] = (byte) i;

            System.out.println(i+") ");
            
            InetAddress address = InetAddress.getByAddress(ip);

            String s = address.getHostAddress();
            System.out.println("  "+address);
            
            if (address.isReachable(250)) {
                // machine is turned on and can be pinged
                System.out.println("  reachable using ping");
                continue;
            }
            
            System.out.println("  checking reverse DNS lookup");
            String s2 = address.getHostName();
            if (!s.equals(s2)) {
                // machine is known in a DNS lookup
                System.out.println("  reachable as "+address.getHostName());
            }
            else {
                System.out.println("  not reachable");
                // the host address and host name are equal, meaning the host name could not be resolved
            }
        }

    }
    
    public static void main(String[] args) throws Exception {
        findAllServers();
    }
    
}
