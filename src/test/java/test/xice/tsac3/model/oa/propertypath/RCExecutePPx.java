// Generated by OABuilder
package test.xice.tsac3.model.oa.propertypath;
 
import java.io.Serializable;

import test.xice.tsac3.model.oa.*;
 
public class RCExecutePPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
    private RCCommandPPx rcCommand;
    private RCInstalledVersionPPx rcInstalledVersions;
     
    public RCExecutePPx(String name) {
        this(null, name);
    }

    public RCExecutePPx(PPxInterface parent, String name) {
        String s = null;
        if (parent != null) {
            s = parent.toString();
        }
        if (s == null) s = "";
        if (name != null) {
            if (s.length() > 0) s += ".";
            s += name;
        }
        pp = s;
    }

    public RCCommandPPx rcCommand() {
        if (rcCommand == null) rcCommand = new RCCommandPPx(this, RCExecute.P_RCCommand);
        return rcCommand;
    }

    public RCInstalledVersionPPx rcInstalledVersions() {
        if (rcInstalledVersions == null) rcInstalledVersions = new RCInstalledVersionPPx(this, RCExecute.P_RCInstalledVersions);
        return rcInstalledVersions;
    }

    public String id() {
        return pp + "." + RCExecute.P_Id;
    }

    public String created() {
        return pp + "." + RCExecute.P_Created;
    }

    public String started() {
        return pp + "." + RCExecute.P_Started;
    }

    public String completed() {
        return pp + "." + RCExecute.P_Completed;
    }

    public String commandLine() {
        return pp + "." + RCExecute.P_CommandLine;
    }

    public String configFileName() {
        return pp + "." + RCExecute.P_ConfigFileName;
    }

    public String input() {
        return pp + "." + RCExecute.P_Input;
    }

    public String output() {
        return pp + "." + RCExecute.P_Output;
    }

    public String error() {
        return pp + "." + RCExecute.P_Error;
    }

    public String console() {
        return pp + "." + RCExecute.P_Console;
    }

    public String processed() {
        return pp + "." + RCExecute.P_Processed;
    }

    public String processingOutput() {
        return pp + "." + RCExecute.P_ProcessingOutput;
    }

    public String loaded() {
        return pp + "." + RCExecute.P_Loaded;
    }

    public String canRun() {
        return pp + "." + RCExecute.P_CanRun;
    }

    public String run() {
        return pp + ".run";
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
