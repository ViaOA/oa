package test.xice.tsam.delegate.oa;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import test.xice.tsam.delegate.oa.CommandDelegate;
import test.xice.tsam.model.oa.Command;
import com.viaoa.hub.Hub;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.OADateTime;

import test.xice.tsam.delegate.RemoteDelegate;
import test.xice.tsam.model.oa.*;
import test.xice.tsam.remote.RemoteMRADInterface;

public class MRADServerCommandDelegate {

    /**
     * create a new instance.
     */
    public static MRADServerCommand create(AdminUser user, Hub<MRADClient> hub, int cmdType) {
        Command command = CommandDelegate.getCommand(cmdType);
        return _create(user, hub, null, command);
    }
    public static MRADServerCommand create(AdminUser user, Hub<MRADClient> hub, Command command) {
        return _create(user, hub, null, command);
    }
    public static MRADServerCommand create(AdminUser user, MRADClient mc, int cmdType) {
        Command command = CommandDelegate.getCommand(cmdType);
        return _create(user, null, mc, command);
    }
    public static MRADServerCommand create(AdminUser user, MRADClient mc, Command command) {
        return _create(user, null, mc, command);
    }
    public static MRADServerCommand create(AdminUser user, int cmdType) {
        Command command = CommandDelegate.getCommand(cmdType);
        return _create(user, null, null, command);
    }
    public static MRADServerCommand create(AdminUser user, Command command) {
        return _create(user, null, null, command);
    }
    
    private static MRADServerCommand _create(AdminUser user, Hub<MRADClient> hub, MRADClient mc, Command command) {
        if (command == null) return null;
        
        MRADServerCommand msc = new MRADServerCommand();
        msc.setAdminUser(user);
        msc.setCommand(command);

        if (hub != null) {
            if (command.getType() != Command.TYPE_UPDATE) {
                for (MRADClient mcx : hub) {
                    MRADClientCommand ccmd = new MRADClientCommand();
                    ccmd.setMRADClient(mcx);
                    msc.getMRADClientCommands().add(ccmd);
                }
            }
        }
        if (mc != null) {
            MRADClientCommand ccmd = new MRADClientCommand();
            ccmd.setMRADClient(mc);
            msc.getMRADClientCommands().add(ccmd);
        }
        return msc;
    }

    /**
     * have a command ran on the RemoteMRADServer
     */
    public static void runOnServer(MRADServerCommand cmd) throws Exception {
        if (cmd == null) return;
        
        if (!OASyncDelegate.isServer()) {
            // send to server
            RemoteDelegate.getRemoteModel().mradServerCommand_runOnServer(cmd);
            return;
        }
    
        //getMRADServerController().runCommand(cmd);
    }

    
    private static LinkedBlockingQueue<MRADServerCommand> queueBlocking = new LinkedBlockingQueue<MRADServerCommand>();
    private static volatile Thread threadQueue;
    
    public static void runOnServerInBackground(MRADServerCommand cmd) throws Exception {
        if (cmd == null) return;

        queueBlocking.add(cmd);
        if (threadQueue == null) {
            synchronized (queueBlocking) {
                if (threadQueue != null) return;
                
                threadQueue = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        for (;;) {
                            try {
                                MRADServerCommand msc = queueBlocking.take();
                                runOnServer(msc);
                            }
                            catch (Exception e) {
                                // logged by command
                            }
                        }
                    }
                }, "MRADServerCommand.queue");
                threadQueue.setDaemon(true);
                threadQueue.start();
            }
        }
    }
    
/*    
    private static MRADServerCommandController mradServerController;
    public static void register(MRADServerCommandController mc) {
        mradServerController = mc;
    }
    public static MRADServerCommandController getMRADServerController() {
        return mradServerController;
    }
*/    
}
