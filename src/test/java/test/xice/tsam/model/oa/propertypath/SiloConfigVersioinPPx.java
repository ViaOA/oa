// Generated by OABuilder
package test.xice.tsam.model.oa.propertypath;
 
import java.io.Serializable;

import test.xice.tsam.model.oa.SiloConfigVersioin;
import test.xice.tsam.model.oa.propertypath.PPxInterface;
import test.xice.tsam.model.oa.propertypath.PackageTypePPx;
import test.xice.tsam.model.oa.propertypath.PackageVersionPPx;
import test.xice.tsam.model.oa.propertypath.SiloConfigPPx;

import test.xice.tsam.model.oa.*;
 
public class SiloConfigVersioinPPx implements PPxInterface, Serializable {
    private static final long serialVersionUID = 1L;
    public final String pp;  // propertyPath
     
    public SiloConfigVersioinPPx(String name) {
        this(null, name);
    }

    public SiloConfigVersioinPPx(PPxInterface parent, String name) {
        String s = null;
        if (parent != null) {
            s = parent.toString();
        }
        if (s == null) s = "";
        if (name != null && name.length() > 0) {
            if (s.length() > 0 && name.charAt(0) != ':') s += ".";
            s += name;
        }
        pp = s;
    }

    public PackageTypePPx packageType() {
        PackageTypePPx ppx = new PackageTypePPx(this, SiloConfigVersioin.P_PackageType);
        return ppx;
    }

    public PackageVersionPPx packageVersion() {
        PackageVersionPPx ppx = new PackageVersionPPx(this, SiloConfigVersioin.P_PackageVersion);
        return ppx;
    }

    public SiloConfigPPx siloConfig() {
        SiloConfigPPx ppx = new SiloConfigPPx(this, SiloConfigVersioin.P_SiloConfig);
        return ppx;
    }

    public String id() {
        return pp + "." + SiloConfigVersioin.P_Id;
    }

    @Override
    public String toString() {
        return pp;
    }
}
 
