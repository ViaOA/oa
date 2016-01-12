package test.theice.tsac.delegate.oa;

import java.awt.Window;
import java.io.File;
import javax.swing.JFileChooser;

import com.viaoa.hub.Hub;
import com.viaoa.jfc.undo.OAUndoManager;
import com.viaoa.object.OAFinder;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.util.OAString;
import com.viaoa.util.OAXMLReader;
import com.viaoa.util.OAXMLWriter;

import test.theice.tsac.delegate.ModelDelegate;
import test.theice.tsac.model.oa.*;
import test.theice.tsac.model.oa.custom.EnvironmentApplication;
import test.theice.tsac.model.oa.propertypath.*;

public class EnvironmentDelegate {

    public static Environment getEnvironment(Site site, String name, boolean bAutoCreate) {
        if (site == null || name == null) return null;

        for (Environment env : site.getEnvironments()) {
            if (name.equalsIgnoreCase(env.getName())) return env;
        }
        if (!bAutoCreate) return null;
        Environment env = new Environment();
        env.setName(name);

        // env type
        for (EnvironmentType et : ModelDelegate.getEnvironmentTypes()) {
            if (name.equalsIgnoreCase(et.getName())) {
                env.setEnvironmentType(et);
                break;
            }
        }

        site.getEnvironments().add(env);
        return env;
    }
    
    
    public static Environment getEnvironment(String hostName, boolean bAutoCreate) {
        if (hostName == null) return null;

        Site site = SiteDelegate.getSite(hostName, false);
        if (site == null) return null;

        String name = OAString.field(hostName, "-", 2);
        if (name == null) {
            if (hostName.toLowerCase().endsWith(".intcx.net")) { // dns name
                name = OAString.field(hostName, ".", 2);
            }
        }
        if (name == null) name = "LOCAL";

        for (Environment env : site.getEnvironments()) {
            if (name.equalsIgnoreCase(env.getName())) return env;
        }
        if (!bAutoCreate) return null;
        Environment env = new Environment();
        env.setName(name);

        // env type
        for (EnvironmentType et : ModelDelegate.getEnvironmentTypes()) {
            if (name.equalsIgnoreCase(et.getName())) {
                env.setEnvironmentType(et);
                break;
            }
        }

        site.getEnvironments().add(env);
        return env;
    }


    public static boolean envExport(Environment env) {
        return true;

    }
    public static boolean envImport(Environment env) {
        return true;
    }
    private static boolean _envImport(Environment env) {
        return true;
    }

}
