// Copied from OATemplate project by OABuilder 09/21/15 03:11 PM
package test.xice.tsam.model.oa.search;

import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.xice.tsam.model.*;
import test.xice.tsam.model.oa.ErrorInfo;

@OAClass(addToCache=false, initialize=true, useDataSource=false, localOnly=true)
public class ConnectionInfoSearch extends OAObject {
    private static final long serialVersionUID = 1L;

    public static final String PROPERTY_UserName = "UserName";
    public static final String PROPERTY_ErrorInfos = "ErrorInfos";

    protected String userName;
    protected ErrorInfo errorInfos;

    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String newValue) {
        String old = userName;
        fireBeforePropertyChange(PROPERTY_UserName, old, newValue);
        this.userName = newValue;
        firePropertyChange(PROPERTY_UserName, old, this.userName);
    }

    public ErrorInfo getErrorInfos() {
        if (errorInfos == null) {
            errorInfos = (ErrorInfo) getObject(PROPERTY_ErrorInfos);
        }
        return errorInfos;
    }
    public void setErrorInfos(ErrorInfo newValue) {
        ErrorInfo old = this.errorInfos;
        fireBeforePropertyChange(PROPERTY_ErrorInfos, old, newValue);
        this.errorInfos = newValue;
        firePropertyChange(PROPERTY_ErrorInfos, old, this.errorInfos);
    }

}
