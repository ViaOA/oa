package test.xice.tsam.delegate.oa;

import test.xice.tsam.model.oa.ApplicationTypeCommand;
import test.xice.tsam.model.oa.Command;
import com.viaoa.util.OAString;

import test.xice.tsam.delegate.ModelDelegate;
import test.xice.tsam.model.oa.*;

public class CommandDelegate {

    public static Command getCommand(int type) {
        for (Command command : ModelDelegate.getCommands()) {
            if (command.getType() == type) {
                return command;
            }
        }
        return null;
    }

    public static String getCommandLine(MRADClient mc, int type) {
        Command command = getCommand(type);
        return getCommandLine(mc, command);
    }

    public static String getCommandLine(MRADClient mc, Command command) {
        if (mc == null) return null;
        if (command == null) return null;

        int type = command.getType();
        String cmdLine = null;

        if (type == Command.TYPE_START) cmdLine = mc.getStartScript();
        else if (type == Command.TYPE_STARTSNAPSHOT) cmdLine = mc.getSnapshotStartScript();
        else if (type == Command.TYPE_STOP) cmdLine = mc.getStopScript();
        if (!OAString.isEmpty(cmdLine)) {
            return cmdLine;
        }
        
        Application app = mc.getApplication();
        if (app == null) return null;

        ApplicationType at = app.getApplicationType();
        if (at == null) return null;

        for (ApplicationTypeCommand ac : at.getApplicationTypeCommands()) {
            if (ac.getCommand() == command) {
                if (ac.getNotSupported()) return null;
                cmdLine = ac.getCommandLine();
                break;
            }
        }
        if (OAString.isEmpty(cmdLine)) {
            cmdLine = command.getCommandLine();
        }
        return cmdLine;
    }

}
