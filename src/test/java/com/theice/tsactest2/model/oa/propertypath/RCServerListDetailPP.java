// Generated by OABuilder
package com.theice.tsactest2.model.oa.propertypath;
 
import com.theice.tsactest2.model.oa.*;
 
public class RCServerListDetailPP {
    private static ApplicationPPx applications;
    private static RCServerListPPx rcServerList;
    private static ServerPPx server;
     

    public static ApplicationPPx applications() {
        if (applications == null) applications = new ApplicationPPx(RCServerListDetail.P_Applications);
        return applications;
    }

    public static RCServerListPPx rcServerList() {
        if (rcServerList == null) rcServerList = new RCServerListPPx(RCServerListDetail.P_RCServerList);
        return rcServerList;
    }

    public static ServerPPx server() {
        if (server == null) server = new ServerPPx(RCServerListDetail.P_Server);
        return server;
    }

    public static String id() {
        String s = RCServerListDetail.P_Id;
        return s;
    }

    public static String hostName() {
        String s = RCServerListDetail.P_HostName;
        return s;
    }

    public static String packages() {
        String s = RCServerListDetail.P_Packages;
        return s;
    }

    public static String invalidMessage() {
        String s = RCServerListDetail.P_InvalidMessage;
        return s;
    }

    public static String selected() {
        String s = RCServerListDetail.P_Selected;
        return s;
    }

    public static String loaded() {
        String s = RCServerListDetail.P_Loaded;
        return s;
    }
}
 
