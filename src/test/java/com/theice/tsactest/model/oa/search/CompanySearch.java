// Generated by OABuilder
package com.theice.tsactest.model.oa.search;

import com.theice.tsactest.model.oa.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.util.filter.OAQueryFilter;
import com.viaoa.ds.*;

@OAClass(useDataSource=false, localOnly=true)
public class CompanySearch extends OAObject {
    private static final long serialVersionUID = 1L;

    public static final String P_Name = "Name";
    public static final String P_ParentCompany = "ParentCompany";
    public static final String P_User = "User";

    protected String name;
    protected Company parentCompany;
    protected User user;

    public String getName() {
        return name;
    }
    
    public void setName(String newValue) {
        fireBeforePropertyChange(P_Name, this.name, newValue);
        String old = name;
        this.name = newValue;
        firePropertyChange(P_Name, old, this.name);
    }
      

    @OAOne
    public Company getParentCompany() {
        if (parentCompany == null) {
            parentCompany = (Company) getObject(P_ParentCompany);
        }
        return parentCompany;
    }
    public void setParentCompany(Company newValue) {
        Company old = this.parentCompany;
        this.parentCompany = newValue;
        firePropertyChange(P_ParentCompany, old, this.parentCompany);
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
        setName(null);
        setParentCompany(null);
        setUser(null);
    }

    public boolean isDataEntered() {
        if (getName() != null) return true;
        if (getParentCompany() != null) return true;
        if (getUser() != null) return true;
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<Company> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<Company> f = new OAQueryFilter<Company>(Company.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<Company> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<Company> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<Company> getSelect() {
        String sql = "";
        String sortOrder = Company.P_Name;
        Object[] args = new Object[0];
        OAFinder finder = null;
        if (!OAString.isEmpty(this.name)) {
            if (sql.length() > 0) sql += " AND ";
            String value = name.replace("*", "%");
            if (!value.endsWith("%")) value += "%";
            if (value.indexOf("%") >= 0) {
                value = value.toUpperCase();
                sql += Company.P_Name + " LIKE ?";
            }
            else {
                sql += Company.P_Name + " = ?";
            }
            args = OAArray.add(Object.class, args, value);
        }
        if (getParentCompany() != null) {
            if (sql.length() > 0) sql += " AND ";
            sql += OAString.cpp(Company.P_ParentCompany) + " = ?";
            args = OAArray.add(Object.class, args, getParentCompany());
            finder = new OAFinder<Company, Company>(getParentCompany(), Company.P_Companies);
        }
        if (getUser() != null) {
            if (sql.length() > 0) sql += " AND ";
            sql += OAString.cpp(Company.P_Users) + " = ?";
            args = OAArray.add(Object.class, args, getUser());
            finder = new OAFinder<User, Company>(getUser(), User.P_Company);
        }

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<Company> sel = new OASelect<Company>(Company.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        sel.setFinder(finder);
        return sel;
    }

    private OAFilter<Company> filterDataSourceFilter;
    public OAFilter<Company> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<Company>() {
            @Override
            public boolean isUsed(Company company) {
                return CompanySearch.this.isUsedForDataSourceFilter(company);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<Company> filterCustomFilter;
    public OAFilter<Company> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<Company>() {
            @Override
            public boolean isUsed(Company company) {
                boolean b = CompanySearch.this.isUsedForCustomFilter(company);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(company);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(Company company) {
        if (name != null) {
            String s = getName();
            if (s != null && s.indexOf('*') < 0 && s.indexOf('%') < 0) s += '*';
            if (!OACompare.isLike(company.getName(), s)) return false;
        }
        if (parentCompany != null) {
            if (!OACompare.isEqual(company.getParentCompany(), parentCompany)) return false;
        }
        if (user != null) {
            if (!OACompare.isIn(user, company.getUsers())) return false;
        }
        return true;
    }
    public boolean isUsedForCustomFilter(Company company) {
        return true;
    }
}
