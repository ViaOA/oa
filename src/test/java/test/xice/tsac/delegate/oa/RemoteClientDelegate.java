package test.xice.tsac.delegate.oa;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.logging.Logger;

import com.viaoa.hub.Hub;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.OADateTime;

import test.xice.tsac.delegate.ModelDelegate;
import test.xice.tsac.delegate.RemoteDelegate;
import test.xice.tsac.model.oa.RemoteClient;
import test.xice.tsac.model.oa.RemoteMessage;

public class RemoteClientDelegate {
    private static Logger LOG = Logger.getLogger(RemoteClientDelegate.class.getName());

    public static RemoteClient getRemoteClient(int type) {
        RemoteClient rc = ModelDelegate.getRemoteClients().find(RemoteClient.PROPERTY_Type, type);
if (rc == null) {
    int xx = 4;
    xx++;
}
        return rc;
    }
    
    public static void addMessage(int type, String msg) {
        addMessage(type, "Status", msg);
    }
    public static void addMessage(int type, String msgName, String msgText) {
        LOG.fine(msgName + ": " + msgText);
        RemoteClient rc = getRemoteClient(type);
        if (rc == null) return;
        
        RemoteMessage msg = new RemoteMessage();
        msg.setName(msgName);
        msg.setMessage(msgText);
        Hub<RemoteMessage> h = rc.getRemoteMessages();
        h.add(msg);
        if (h.getSize() > 1000) {
            h.removeAt(0);
        }
    }
    
    public static void addMessage(RemoteClient rc, String msg) {
        addMessage(rc, "Status", msg);
    }
    public static void addMessage(RemoteClient rc, String msgName, String msgText) {
        LOG.fine(msgName + ": " + msgText);
        if (rc == null) return;
        
        RemoteMessage msg = new RemoteMessage();
        msg.setName(msgName);
        msg.setMessage(msgText);
        Hub<RemoteMessage> h = rc.getRemoteMessages();
        h.add(msg);
        if (h.getSize() > 1000) {
            h.removeAt(0);
        }
    }


    public static void start(RemoteClient ri) throws Exception {
        if (!OASyncDelegate.isServer()) {
            return;
        }

        String cmd = "";
        
        switch (ri.getType()) {
        case RemoteClient.TYPE_RC:
            cmd = "./rcclient.sh start";
            break;
        case RemoteClient.TYPE_MRAD:
            cmd = "./mradclient.sh start";
            break;
            
//qqqqqqqqqqq more remote clients need to be added here qqqqqqqqqqq            
        }

        String[] cmds;
        String osName = System.getProperty("os.name");        
        if (osName != null && osName.toLowerCase().startsWith("windows")) {
            // dos:
            cmds = new String[] {"cmd", "/c", cmd};
            ri.setConsole("using DOS cmd /c");
        }
        else {
            // unix:
            cmds = new String[] {"bash", "-c", cmd};
            ri.setConsole("user bash -c");
        }
        ri.setConsole(cmd);
        
        
        ProcessBuilder pb = new ProcessBuilder(cmds);
        pb.redirectErrorStream(true);

        Process pr = pb.start();

        BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
        String output = null;
        for (int i=1 ;; i++) {
            String s = in.readLine();
            if (s == null) break;
            ri.setConsole(s);
            LOG.fine("line="+s);
            if (output == null) output = "";
            else output += "\n";
            output += s;
        }

        // execute.setOutput(output);
        in.close();
        
        pr.waitFor();
        in.close();
        
        
    }
    public static void stop(RemoteClient ri) throws Exception {
        if (ri != null) ri.setStatus(ri.STATUS_Disconnected);
    }
}



