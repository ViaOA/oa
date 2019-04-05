package test.xice.tsac.delegate.oa;

import com.viaoa.remote.multiplexer.OARemoteThreadDelegate;
import com.viaoa.sync.OASyncDelegate;
import com.viaoa.util.OAString;

import test.xice.tsac.delegate.ModelDelegate;
import test.xice.tsac.model.oa.*;

public class RCCommandDelegate {

    public static RCCommand getExecuteCommand(int type) {
        RCCommand ec = ModelDelegate.getRCCommands().find(RCCommand.P_Type, type);
        return ec;
    }

    public static void initialize(RCCommand cmd) {
        if (cmd == null) return;
        if (!OAString.isEmpty(cmd.getCommandLine())) return;
        if (!OASyncDelegate.isServer()) return;
        try {
            OARemoteThreadDelegate.sendMessages(true);
            _initialize(cmd);
        }
        finally {
            OARemoteThreadDelegate.sendMessages(false);
        }
    }
        
    private static void _initialize(RCCommand cmd) {
        switch (cmd.getType()) {
        case RCCommand.TYPE_download: 
            cmd.setCommandLine("LANG=en_US.UTF-8 PATH=$HOME/.local/bin:$PATH rc-deploy --json download -e $ENV /home/tsacadmin/remote_control/config/tsac_deployment.yaml");
            break;
        case RCCommand.TYPE_stage: 
            cmd.setCommandLine("LANG=en_US.UTF-8 PATH=$HOME/.local/bin:$PATH rc-deploy --json stage -e $ENV /home/tsacadmin/remote_control/config/tsac_deployment.yaml");
            break;
        case RCCommand.TYPE_install: 
            cmd.setCommandLine("LANG=en_US.UTF-8 PATH=$HOME/.local/bin:$PATH rc-deploy --json install -e $ENV /home/tsacadmin/remote_control/config/tsac_deployment.yaml");
            break;
        case RCCommand.TYPE_getRepoVersions: 
            cmd.setCommandLine("LANG=en_US.UTF-8 PATH=$HOME/.local/bin:$PATH rc-version --json repo-list");
            break;
        case RCCommand.TYPE_getInstalledVersions: 
            cmd.setCommandLine("LANG=en_US.UTF-8 PATH=$HOME/.local/bin:$PATH rc-version --json list -e tsac_temp");
            break;
        case RCCommand.TYPE_getServiceList: 
            cmd.setCommandLine("cat /home/tsacadmin/remote_control/config/rc/services.yaml");
            break;
        case RCCommand.TYPE_getServerList: 
            cmd.setCommandLine("cat /home/tsacadmin/remote_control/config/rc/$ENV/hosts.yaml");
            break;
        case RCCommand.TYPE_getPackageList: 
            cmd.setCommandLine("cat /home/tsacadmin/remote_control/config/rc/packages.yaml");
            break;
        case RCCommand.TYPE_start: 
            cmd.setCommandLine("LANG=en_US.UTF-8 PATH=$HOME/.local/bin:$PATH rc-admin --json start -e $ENV -s $APPS");
            break;
        case RCCommand.TYPE_stop: 
            cmd.setCommandLine("LANG=en_US.UTF-8 PATH=$HOME/.local/bin:$PATH rc-admin --json stop -e $ENV -s $APPS");
            break;
        }
    }
}
