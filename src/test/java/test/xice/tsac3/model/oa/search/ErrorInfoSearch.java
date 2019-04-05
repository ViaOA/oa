// Copied from OATemplate project by OABuilder 09/10/14 05:33 PM
package test.xice.tsac3.model.oa.search;

import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;

import test.xice.tsac3.model.*;
import test.xice.tsac3.model.oa.ConnectionInfo;
import test.xice.tsac3.model.oa.ExceptionType;

@OAClass(addToCache=false, initialize=true, useDataSource=false, localOnly=true)
public class ErrorInfoSearch extends OAObject {
    private static final long serialVersionUID = 1L;

    public static final String PROPERTY_ConnectionInfo = "ConnectionInfo";
    public static final String PROPERTY_ExceptionType = "ExceptionType";

    protected ConnectionInfo connectionInfo;
    protected ExceptionType exceptionType;

    public ConnectionInfo getConnectionInfo() {
        if (connectionInfo == null) {
            connectionInfo = (ConnectionInfo) getObject(PROPERTY_ConnectionInfo);
        }
        return connectionInfo;
    }
    public void setConnectionInfo(ConnectionInfo newValue) {
        ConnectionInfo old = this.connectionInfo;
        fireBeforePropertyChange(PROPERTY_ConnectionInfo, old, newValue);
        this.connectionInfo = newValue;
        firePropertyChange(PROPERTY_ConnectionInfo, old, this.connectionInfo);
    }

    public ExceptionType getExceptionType() {
        if (exceptionType == null) {
            exceptionType = (ExceptionType) getObject(PROPERTY_ExceptionType);
        }
        return exceptionType;
    }
    public void setExceptionType(ExceptionType newValue) {
        ExceptionType old = this.exceptionType;
        fireBeforePropertyChange(PROPERTY_ExceptionType, old, newValue);
        this.exceptionType = newValue;
        firePropertyChange(PROPERTY_ExceptionType, old, this.exceptionType);
    }

}
