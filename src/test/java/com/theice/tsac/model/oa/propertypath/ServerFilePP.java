// Generated by OABuilder
package com.theice.tsac.model.oa.propertypath;
 
import com.theice.tsac.model.oa.*;
 
public class ServerFilePP {
    private static ServerPPx server;
     

    public static ServerPPx server() {
        if (server == null) server = new ServerPPx(ServerFile.P_Server);
        return server;
    }

    public static String id() {
        String s = ServerFile.P_Id;
        return s;
    }

    public static String created() {
        String s = ServerFile.P_Created;
        return s;
    }

    public static String fileName() {
        String s = ServerFile.P_FileName;
        return s;
    }

    public static String fileType() {
        String s = ServerFile.P_FileType;
        return s;
    }

    public static String dateTime() {
        String s = ServerFile.P_DateTime;
        return s;
    }

    public static String length() {
        String s = ServerFile.P_Length;
        return s;
    }

    public static String currentFilePos() {
        String s = ServerFile.P_CurrentFilePos;
        return s;
    }

    public static String notes() {
        String s = ServerFile.P_Notes;
        return s;
    }

    public static String isLoaded() {
        String s = ServerFile.P_IsLoaded;
        return s;
    }

    public static String readFile() {
        String s = "readFile";
        return s;
    }
}
 
