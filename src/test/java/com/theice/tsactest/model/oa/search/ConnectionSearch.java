// Copied from OATemplate project by OABuilder 09/10/14 05:33 PM
package com.theice.tsactest.model.oa.search;

import com.theice.tsactest.model.*;
import com.theice.tsactest.model.oa.ServerInfo;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

@OAClass(addToCache=false, initialize=true, useDataSource=false, localOnly=true)
public class ConnectionSearch extends OAObject {
    private static final long serialVersionUID = 1L;

    public static final String PROPERTY_UserName = "UserName";
    public static final String PROPERTY_ServerInfo = "ServerInfo";

    protected String userName;
    protected ServerInfo serverInfo;

    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String newValue) {
        String old = userName;
        fireBeforePropertyChange(PROPERTY_UserName, old, newValue);
        this.userName = newValue;
        firePropertyChange(PROPERTY_UserName, old, this.userName);
    }

    public ServerInfo getServerInfo() {
        if (serverInfo == null) {
            serverInfo = (ServerInfo) getObject(PROPERTY_ServerInfo);
        }
        return serverInfo;
    }
    
    public void setServerInfo(ServerInfo newValue) {
        ServerInfo old = this.serverInfo;
        fireBeforePropertyChange(PROPERTY_ServerInfo, old, newValue);
        this.serverInfo = newValue;
        firePropertyChange(PROPERTY_ServerInfo, old, this.serverInfo);
    }

}
