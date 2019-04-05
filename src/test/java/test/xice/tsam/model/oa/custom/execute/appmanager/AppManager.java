package test.xice.tsam.model.oa.custom.execute.appmanager;

import com.viaoa.object.*;
import com.viaoa.annotation.OACalculatedProperty;
import com.viaoa.annotation.OAClass;
import com.viaoa.hub.*;
import com.viaoa.util.*;

/**
 * Used by CustomMRADJfc for UI specific data.
 * @author vvia
 *
 */
@OAClass(addToCache=false, localOnly=true, useDataSource=false)
public class AppManager extends OAObject {

    public static final String P_Toggle = "Toggle";
    public static final String P_IsMROrHistoryToggle = "IsMROrHistoryToggle";
    public static final String P_IsTeOrCeToggle = "IsTeOrCeToggle";
    
    
    // keeps track of which toggle is active:
    public final static int Toggle_MRs = 0;
    public final static int Toggle_TEs = 1;
    public final static int Toggle_CMs = 2;
    public final static int Toggle_History = 3;
    protected int toggle;

    public int getToggle() {
        return toggle;
    }
    public void setToggle(int newValue) {
        fireBeforePropertyChange(P_Toggle, this.toggle, newValue);
        int old = this.toggle;
        this.toggle = newValue;
        firePropertyChange(P_Toggle, old, this.toggle);
    }
 
    @OACalculatedProperty(properties=P_Toggle)
    public boolean getIsMROrHistoryToggle() {
        int x = getToggle();
        return x == Toggle_MRs || x == Toggle_History;
    }
    @OACalculatedProperty(properties=P_Toggle)
    public boolean getIsTeOrCeToggle() {
        int x = getToggle();
        return x == Toggle_TEs || x == Toggle_CMs;
    }
    
}
