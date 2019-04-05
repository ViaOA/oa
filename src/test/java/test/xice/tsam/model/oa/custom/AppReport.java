// Copied from OATemplate project by OABuilder 09/21/15 03:11 PM
package test.xice.tsam.model.oa.custom;

import com.viaoa.annotation.OAClass;
import com.viaoa.object.OAObject;

@OAClass(addToCache=false, initialize=false, useDataSource=false, localOnly=true)
public class AppReport extends OAObject {
    public static final String PROPERTY_Name = "Name";
    protected String name;

    public AppReport() {
    }
    public AppReport(String name, String displayName) {
        setName(name);
        setDisplayName(displayName);
    }
    
    public String getName() {
        return name;
    }

    public void setName(String newValue) {
        String old = this.name;
        fireBeforePropertyChange(PROPERTY_Name, old, this.name);
        this.name = newValue;
        firePropertyChange(PROPERTY_Name, old, this.name);
    }

    public static final String PROPERTY_DisplayName = "DisplayName";
    protected String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String newValue) {
        String old = this.displayName;
        this.displayName = newValue;
        firePropertyChange(PROPERTY_DisplayName, old, this.displayName);
    }

}
