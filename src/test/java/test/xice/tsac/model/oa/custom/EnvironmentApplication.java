package test.xice.tsac.model.oa.custom;

import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;


/**
 * This is a temp used for import/export for an Environment
 * @author vvia
 *
 */
@OAClass(localOnly = true, useDataSource = false, addToCache = false)
public class EnvironmentApplication extends OAObject {
    private static final long serialVersionUID = 1L;

    public static final String P_SiloType = "SiloType";
    public static final String P_HostName = "HostName";
    public static final String P_AppTypeCode = "AppTypeCode";
    public static final String P_PackageCode = "PackageCode";
    public static final String P_Version = "Version";

    protected int siloType;
    protected String hostName;
    protected String appTypeCode;
    protected String packageCode;
    protected String version;

    public int getSiloType() {
        return siloType;
    }
    public void setSiloType(int newValue) {
        fireBeforePropertyChange(P_SiloType, this.siloType, newValue);
        int old = this.siloType;
        this.siloType = newValue;
        firePropertyChange(P_SiloType, old, this.siloType);
    }
    
    public String getHostName() {
        return hostName;
    }
    public void setHostName(String newValue) {
        fireBeforePropertyChange(P_HostName, this.hostName, newValue);
        String old = this.hostName;
        this.hostName = newValue;
        firePropertyChange(P_HostName, old, this.hostName);
    }

    public String getAppTypeCode() {
        return appTypeCode;
    }
    public void setAppTypeCode(String newValue) {
        fireBeforePropertyChange(P_AppTypeCode, this.appTypeCode, newValue);
        String old = this.appTypeCode;
        this.appTypeCode = newValue;
        firePropertyChange(P_AppTypeCode, old, this.appTypeCode);
    }
    
    public String getPackageCode() {
        return packageCode;
    }
    public void setPackageCode(String newValue) {
        fireBeforePropertyChange(P_PackageCode, this.packageCode, newValue);
        String old = this.packageCode;
        this.packageCode = newValue;
        firePropertyChange(P_PackageCode, old, this.packageCode);
    }

    public String getVersion() {
        return version;
    }
    public void setVersion(String newValue) {
        fireBeforePropertyChange(P_Version, this.version, newValue);
        String old = this.version;
        this.version = newValue;
        firePropertyChange(P_Version, old, this.version);
    }
    
}
