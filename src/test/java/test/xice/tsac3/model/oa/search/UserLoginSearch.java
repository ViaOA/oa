// Generated by OABuilder
package test.xice.tsac3.model.oa.search;

import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.util.filter.OAQueryFilter;

import test.xice.tsac3.model.oa.*;

import com.viaoa.ds.*;

@OAClass(useDataSource=false, localOnly=true)
public class UserLoginSearch extends OAObject {
    private static final long serialVersionUID = 1L;

    public static final String P_UserUserId = "UserUserId";
    public static final String P_UserFirstName = "UserFirstName";
    public static final String P_UserLastName = "UserLastName";
    public static final String P_Gateway = "Gateway";
    public static final String P_LLADClientServer = "LLADClientServer";
    public static final String P_ClientAppType = "ClientAppType";

    protected String userUserId;
    protected String userFirstName;
    protected String userLastName;
    protected String gateway;
    protected Server lladClientServer;
    protected ClientAppType clientAppType;

    public String getUserUserId() {
        return userUserId;
    }
    
    public void setUserUserId(String newValue) {
        fireBeforePropertyChange(P_UserUserId, this.userUserId, newValue);
        String old = userUserId;
        this.userUserId = newValue;
        firePropertyChange(P_UserUserId, old, this.userUserId);
    }
      

    public String getUserFirstName() {
        return userFirstName;
    }
    
    public void setUserFirstName(String newValue) {
        fireBeforePropertyChange(P_UserFirstName, this.userFirstName, newValue);
        String old = userFirstName;
        this.userFirstName = newValue;
        firePropertyChange(P_UserFirstName, old, this.userFirstName);
    }
      

    public String getUserLastName() {
        return userLastName;
    }
    
    public void setUserLastName(String newValue) {
        fireBeforePropertyChange(P_UserLastName, this.userLastName, newValue);
        String old = userLastName;
        this.userLastName = newValue;
        firePropertyChange(P_UserLastName, old, this.userLastName);
    }
      

    public String getGateway() {
        return gateway;
    }
    
    public void setGateway(String newValue) {
        fireBeforePropertyChange(P_Gateway, this.gateway, newValue);
        String old = gateway;
        this.gateway = newValue;
        firePropertyChange(P_Gateway, old, this.gateway);
    }
      

    @OAOne
    public Server getLLADClientServer() {
        if (lladClientServer == null) {
            lladClientServer = (Server) getObject(P_LLADClientServer);
        }
        return lladClientServer;
    }
    public void setLLADClientServer(Server newValue) {
        Server old = this.lladClientServer;
        this.lladClientServer = newValue;
        firePropertyChange(P_LLADClientServer, old, this.lladClientServer);
    }

    @OAOne
    public ClientAppType getClientAppType() {
        if (clientAppType == null) {
            clientAppType = (ClientAppType) getObject(P_ClientAppType);
        }
        return clientAppType;
    }
    public void setClientAppType(ClientAppType newValue) {
        ClientAppType old = this.clientAppType;
        this.clientAppType = newValue;
        firePropertyChange(P_ClientAppType, old, this.clientAppType);
    }

    public void reset() {
        setUserUserId(null);
        setUserFirstName(null);
        setUserLastName(null);
        setGateway(null);
        setLLADClientServer(null);
        setClientAppType(null);
    }

    public boolean isDataEntered() {
        if (getUserUserId() != null) return true;
        if (getUserFirstName() != null) return true;
        if (getUserLastName() != null) return true;
        if (getGateway() != null) return true;
        if (getLLADClientServer() != null) return true;
        if (getClientAppType() != null) return true;
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<UserLogin> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<UserLogin> f = new OAQueryFilter<UserLogin>(UserLogin.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<UserLogin> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<UserLogin> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<UserLogin> getSelect() {
        String sql = "";
        String sortOrder = OAString.cpp(UserLogin.P_User, User.P_LastName);
        Object[] args = new Object[0];
        if (!OAString.isEmpty(this.userUserId)) {
            if (sql.length() > 0) sql += " AND ";
            String value = userUserId.replace("*", "%");
            if (!value.endsWith("%")) value += "%";
            if (value.indexOf("%") >= 0) {
                value = value.toUpperCase();
                sql += OAString.cpp(UserLogin.P_User, User.P_UserId) + " LIKE ?";
            }
            else {
                sql += OAString.cpp(UserLogin.P_User, User.P_UserId) + " = ?";
            }
            args = OAArray.add(Object.class, args, value);
        }
        if (!OAString.isEmpty(this.userFirstName)) {
            if (sql.length() > 0) sql += " AND ";
            String value = userFirstName.replace("*", "%");
            if (!value.endsWith("%")) value += "%";
            if (value.indexOf("%") >= 0) {
                value = value.toUpperCase();
                sql += OAString.cpp(UserLogin.P_User, User.P_FirstName) + " LIKE ?";
            }
            else {
                sql += OAString.cpp(UserLogin.P_User, User.P_FirstName) + " = ?";
            }
            args = OAArray.add(Object.class, args, value);
        }
        if (!OAString.isEmpty(this.userLastName)) {
            if (sql.length() > 0) sql += " AND ";
            String value = userLastName.replace("*", "%");
            if (!value.endsWith("%")) value += "%";
            if (value.indexOf("%") >= 0) {
                value = value.toUpperCase();
                sql += OAString.cpp(UserLogin.P_User, User.P_LastName) + " LIKE ?";
            }
            else {
                sql += OAString.cpp(UserLogin.P_User, User.P_LastName) + " = ?";
            }
            args = OAArray.add(Object.class, args, value);
        }
        if (!OAString.isEmpty(this.gateway)) {
            if (sql.length() > 0) sql += " AND ";
            String value = gateway.replace("*", "%");
            if (!value.endsWith("%")) value += "%";
            if (value.indexOf("%") >= 0) {
                value = value.toUpperCase();
                sql += UserLogin.P_Gateway + " LIKE ?";
            }
            else {
                sql += UserLogin.P_Gateway + " = ?";
            }
            args = OAArray.add(Object.class, args, value);
        }
        if (getLLADClientServer() != null) {
            if (sql.length() > 0) sql += " AND ";
            sql += OAString.cpp(UserLogin.P_LLADClient, LLADClient.P_Server) + " = ?";
            args = OAArray.add(Object.class, args, getLLADClientServer());
        }
        if (getClientAppType() != null) {
            if (sql.length() > 0) sql += " AND ";
            sql += OAString.cpp(UserLogin.P_ClientAppType) + " = ?";
            args = OAArray.add(Object.class, args, getClientAppType());
        }

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<UserLogin> sel = new OASelect<UserLogin>(UserLogin.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<UserLogin> filterDataSourceFilter;
    public OAFilter<UserLogin> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<UserLogin>() {
            @Override
            public boolean isUsed(UserLogin userLogin) {
                return UserLoginSearch.this.isUsedForDataSourceFilter(userLogin);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<UserLogin> filterCustomFilter;
    public OAFilter<UserLogin> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<UserLogin>() {
            @Override
            public boolean isUsed(UserLogin userLogin) {
                boolean b = UserLoginSearch.this.isUsedForCustomFilter(userLogin);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(userLogin);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(UserLogin userLogin) {
        if (userUserId != null) {
            String s = getUserUserId();
            if (s != null && s.indexOf('*') < 0 && s.indexOf('%') < 0) s += '*';
            if (!OACompare.isLike(userLogin.getProperty(OAString.cpp(UserLogin.P_User, User.P_UserId)), s)) return false;
        }
        if (userFirstName != null) {
            String s = getUserFirstName();
            if (s != null && s.indexOf('*') < 0 && s.indexOf('%') < 0) s += '*';
            if (!OACompare.isLike(userLogin.getProperty(OAString.cpp(UserLogin.P_User, User.P_FirstName)), s)) return false;
        }
        if (userLastName != null) {
            String s = getUserLastName();
            if (s != null && s.indexOf('*') < 0 && s.indexOf('%') < 0) s += '*';
            if (!OACompare.isLike(userLogin.getProperty(OAString.cpp(UserLogin.P_User, User.P_LastName)), s)) return false;
        }
        if (lladClientServer != null) {
            if (!OACompare.isEqual(userLogin.getProperty(OAString.cpp(UserLogin.P_LLADClient, LLADClient.P_Server)), lladClientServer)) return false;
        }
        if (gateway != null) {
            String s = getGateway();
            if (s != null && s.indexOf('*') < 0 && s.indexOf('%') < 0) s += '*';
            if (!OACompare.isLike(userLogin.getGateway(), s)) return false;
        }
        if (clientAppType != null) {
            if (!OACompare.isEqual(userLogin.getClientAppType(), clientAppType)) return false;
        }
        return true;
    }
    public boolean isUsedForCustomFilter(UserLogin userLogin) {
        return true;
    }
}
