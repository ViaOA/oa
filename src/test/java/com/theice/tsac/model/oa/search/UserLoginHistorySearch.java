// Generated by OABuilder
package com.theice.tsac.model.oa.search;

import com.theice.tsac.model.oa.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.util.OADateTime;
import com.viaoa.ds.*;

@OAClass(useDataSource=false, localOnly=true)
public class UserLoginHistorySearch extends OAObject {
    private static final long serialVersionUID = 1L;

    public static final String P_Login = "Login";
    public static final String P_LoginUseNull = "LoginUseNull";
    public static final String P_LoginUseNotNull = "LoginUseNotNull";
    public static final String P_User = "User";

    protected OADateTime login;
    protected boolean loginUseNull;
    protected boolean loginUseNotNull;
    protected User user;

    public OADateTime getLogin() {
        return login;
    }
    
    public void setLogin(OADateTime newValue) {
        fireBeforePropertyChange(P_Login, this.login, newValue);
        OADateTime old = login;
        this.login = newValue;
        firePropertyChange(P_Login, old, this.login);
    }
      
    public boolean getLoginUseNull() {
        return loginUseNull;
    }
    public void setLoginUseNull(boolean newValue) {
        boolean old = this.loginUseNull;
        this.loginUseNull = newValue;
        firePropertyChange(P_LoginUseNull, old, this.loginUseNull);
    }
    public boolean getLoginUseNotNull() {
        return loginUseNotNull;
    }
    public void setLoginUseNotNull(boolean newValue) {
        boolean old = this.loginUseNotNull;
        this.loginUseNotNull = newValue;
        firePropertyChange(P_LoginUseNotNull, old, this.loginUseNotNull);
    }

    @OAOne
    public User getUser() {
        if (user == null) {
            user = (User) getObject(P_User);
        }
        return user;
    }
    public void setUser(User newValue) {
        User old = this.user;
        this.user = newValue;
        firePropertyChange(P_User, old, this.user);
    }

    public void reset() {
        setLogin(null);
        setLoginUseNull(false);
        setLoginUseNotNull(false);
        setUser(null);
    }

    public boolean isDataEntered() {
        if (getLogin() != null) return true;
        if (getLoginUseNull()) return true;
        if (getLoginUseNull()) return true;
        if (getLoginUseNull()) return true;
        if (getLoginUseNotNull()) return true;
        if (getUser() != null) return true;
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<UserLoginHistory> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<UserLoginHistory> f = new OASelectFilter<UserLoginHistory>(UserLoginHistory.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<UserLoginHistory> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<UserLoginHistory> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<UserLoginHistory> getSelect() {
        String sql = "";
        String sortOrder = "";
        Object[] args = new Object[0];
        OAFinder finder = null;
        if (loginUseNull) {
            if (sql.length() > 0) sql += " AND ";
            sql += UserLoginHistory.P_Login + " = null";
        }
        else if (loginUseNotNull) {
            if (sql.length() > 0) sql += " AND ";
            sql += UserLoginHistory.P_Login + " != null";
        }
        else if (login != null) {
            if (sql.length() > 0) sql += " AND ";
            sql += UserLoginHistory.P_Login + " = ?";
            args = OAArray.add(Object.class, args, this.login);
        }
        if (getUser() != null) {
            if (sql.length() > 0) sql += " AND ";
            sql += OAString.cpp(UserLoginHistory.P_User) + " = ?";
            args = OAArray.add(Object.class, args, getUser());
            finder = new OAFinder<User, UserLoginHistory>(getUser(), User.P_UserLoginHistories);
        }

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<UserLoginHistory> sel = new OASelect<UserLoginHistory>(UserLoginHistory.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        sel.setFinder(finder);
        return sel;
    }

    private OAFilter<UserLoginHistory> filterDataSourceFilter;
    public OAFilter<UserLoginHistory> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<UserLoginHistory>() {
            @Override
            public boolean isUsed(UserLoginHistory userLoginHistory) {
                return UserLoginHistorySearch.this.isUsedForDataSourceFilter(userLoginHistory);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<UserLoginHistory> filterCustomFilter;
    public OAFilter<UserLoginHistory> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<UserLoginHistory>() {
            @Override
            public boolean isUsed(UserLoginHistory userLoginHistory) {
                boolean b = UserLoginHistorySearch.this.isUsedForCustomFilter(userLoginHistory);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(userLoginHistory);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(UserLoginHistory userLoginHistory) {
        if (user != null) {
            if (!OACompare.isEqual(userLoginHistory.getUser(), user)) return false;
        }
        if (loginUseNull) {
            if (!OACompare.isEmpty(userLoginHistory.getLogin())) return false;
        }
        else if (loginUseNotNull) {
            if (OACompare.isEmpty(userLoginHistory.getLogin())) return false;
        }
        else if (login != null) {
            if (!OACompare.isEqual(userLoginHistory.getLogin(), login)) return false;
        }
        return true;
    }
    public boolean isUsedForCustomFilter(UserLoginHistory userLoginHistory) {
        return true;
    }
}
