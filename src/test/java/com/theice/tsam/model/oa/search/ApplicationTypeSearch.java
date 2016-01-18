// Generated by OABuilder
package com.theice.tsam.model.oa.search;

import java.util.logging.*;
import com.theice.tsam.model.oa.*;
import com.theice.tsam.model.oa.propertypath.*;
import com.viaoa.annotation.*;
import com.viaoa.object.*;
import com.viaoa.hub.*;
import com.viaoa.util.*;
import com.viaoa.ds.*;
import com.viaoa.util.filter.OAQueryFilter;

@OAClass(useDataSource=false, localOnly=true)
public class ApplicationTypeSearch extends OAObject {
    private static final long serialVersionUID = 1L;
    private static Logger LOG = Logger.getLogger(ApplicationTypeSearch.class.getName());
    public static final String P_Code = "Code";
    public static final String P_CodeUseNull = "CodeUseNull";
    public static final String P_CodeUseNotNull = "CodeUseNotNull";
    public static final String P_DnsName = "DnsName";
    public static final String P_UsesCron = "UsesCron";
    public static final String P_UsesPool = "UsesPool";
    public static final String P_UsesDns = "UsesDns";
    public static final String P_DNSShortName = "DNSShortName";
    public static final String P_ClientPort = "ClientPort";
    public static final String P_WebPort = "WebPort";
    public static final String P_VipClientPort = "VipClientPort";
    public static final String P_VipClientPort2 = "VipClientPort2";
    public static final String P_VipWebPort = "VipWebPort";
    public static final String P_VipWebPort2 = "VipWebPort2";
    public static final String P_UsesIDL = "UsesIDL";
    public static final String P_ConnectsToMRAD = "ConnectsToMRAD";

    protected String code;
    protected boolean codeUseNull;
    protected boolean codeUseNotNull;
    protected String dnsName;
    protected boolean usesCron;
    protected boolean usesPool;
    protected boolean usesDns;
    protected String dnsShortName;
    protected int clientPort;
    protected int webPort;
    protected int vipClientPort;
    protected int vipClientPort2;
    protected int vipWebPort;
    protected int vipWebPort2;
    protected boolean usesIDL;
    protected boolean connectsToMRAD;

    public String getCode() {
        return code;
    }
    public void setCode(String newValue) {
        fireBeforePropertyChange(P_Code, this.code, newValue);
        String old = code;
        this.code = newValue;
        firePropertyChange(P_Code, old, this.code);
    }
    
      
    public boolean getCodeUseNull() {
        return codeUseNull;
    }
    public void setCodeUseNull(boolean newValue) {
        boolean old = this.codeUseNull;
        this.codeUseNull = newValue;
        firePropertyChange(P_CodeUseNull, old, this.codeUseNull);
    }
    public boolean getCodeUseNotNull() {
        return codeUseNotNull;
    }
    public void setCodeUseNotNull(boolean newValue) {
        boolean old = this.codeUseNotNull;
        this.codeUseNotNull = newValue;
        firePropertyChange(P_CodeUseNotNull, old, this.codeUseNotNull);
    }

    public String getDnsName() {
        return dnsName;
    }
    public void setDnsName(String newValue) {
        fireBeforePropertyChange(P_DnsName, this.dnsName, newValue);
        String old = dnsName;
        this.dnsName = newValue;
        firePropertyChange(P_DnsName, old, this.dnsName);
    }
    
      

    /**
      Is cron used to start this server?
    */
    public boolean getUsesCron() {
        return usesCron;
    }
    public void setUsesCron(boolean newValue) {
        fireBeforePropertyChange(P_UsesCron, this.usesCron, newValue);
        boolean old = usesCron;
        this.usesCron = newValue;
        firePropertyChange(P_UsesCron, old, this.usesCron);
    }
    
      

    /**
      is there a pool of servers
    */
    public boolean getUsesPool() {
        return usesPool;
    }
    public void setUsesPool(boolean newValue) {
        fireBeforePropertyChange(P_UsesPool, this.usesPool, newValue);
        boolean old = usesPool;
        this.usesPool = newValue;
        firePropertyChange(P_UsesPool, old, this.usesPool);
    }
    
      

    public boolean getUsesDns() {
        return usesDns;
    }
    public void setUsesDns(boolean newValue) {
        fireBeforePropertyChange(P_UsesDns, this.usesDns, newValue);
        boolean old = usesDns;
        this.usesDns = newValue;
        firePropertyChange(P_UsesDns, old, this.usesDns);
    }
    
      

    /**
      template used to describe DNS name for this server
    */
    public String getDNSShortName() {
        return dnsShortName;
    }
    public void setDNSShortName(String newValue) {
        fireBeforePropertyChange(P_DNSShortName, this.dnsShortName, newValue);
        String old = dnsShortName;
        this.dnsShortName = newValue;
        firePropertyChange(P_DNSShortName, old, this.dnsShortName);
    }
    
      

    public int getClientPort() {
        return clientPort;
    }
    public void setClientPort(int newValue) {
        fireBeforePropertyChange(P_ClientPort, this.clientPort, newValue);
        int old = clientPort;
        this.clientPort = newValue;
        firePropertyChange(P_ClientPort, old, this.clientPort);
    }
    
      

    public int getWebPort() {
        return webPort;
    }
    public void setWebPort(int newValue) {
        fireBeforePropertyChange(P_WebPort, this.webPort, newValue);
        int old = webPort;
        this.webPort = newValue;
        firePropertyChange(P_WebPort, old, this.webPort);
    }
    
      

    public int getVipClientPort() {
        return vipClientPort;
    }
    public void setVipClientPort(int newValue) {
        fireBeforePropertyChange(P_VipClientPort, this.vipClientPort, newValue);
        int old = vipClientPort;
        this.vipClientPort = newValue;
        firePropertyChange(P_VipClientPort, old, this.vipClientPort);
        if (isLoading()) return;
        if (vipClientPort > vipClientPort2) setVipClientPort2(this.vipClientPort);
    } 
    public int getVipClientPort2() {
        return vipClientPort2;
    }
    public void setVipClientPort2(int newValue) {
        fireBeforePropertyChange(P_VipClientPort2, this.vipClientPort2, newValue);
        int old = vipClientPort2;
        this.vipClientPort2 = newValue;
        firePropertyChange(P_VipClientPort2, old, this.vipClientPort2);
        if (isLoading()) return;
        if (vipClientPort > vipClientPort2) setVipClientPort(this.vipClientPort2);
    }

    public int getVipWebPort() {
        return vipWebPort;
    }
    public void setVipWebPort(int newValue) {
        fireBeforePropertyChange(P_VipWebPort, this.vipWebPort, newValue);
        int old = vipWebPort;
        this.vipWebPort = newValue;
        firePropertyChange(P_VipWebPort, old, this.vipWebPort);
        if (isLoading()) return;
        if (vipWebPort > vipWebPort2) setVipWebPort2(this.vipWebPort);
    } 
    public int getVipWebPort2() {
        return vipWebPort2;
    }
    public void setVipWebPort2(int newValue) {
        fireBeforePropertyChange(P_VipWebPort2, this.vipWebPort2, newValue);
        int old = vipWebPort2;
        this.vipWebPort2 = newValue;
        firePropertyChange(P_VipWebPort2, old, this.vipWebPort2);
        if (isLoading()) return;
        if (vipWebPort > vipWebPort2) setVipWebPort(this.vipWebPort2);
    }

    public boolean getUsesIDL() {
        return usesIDL;
    }
    public void setUsesIDL(boolean newValue) {
        fireBeforePropertyChange(P_UsesIDL, this.usesIDL, newValue);
        boolean old = usesIDL;
        this.usesIDL = newValue;
        firePropertyChange(P_UsesIDL, old, this.usesIDL);
    }
    
      

    public boolean getConnectsToMRAD() {
        return connectsToMRAD;
    }
    public void setConnectsToMRAD(boolean newValue) {
        fireBeforePropertyChange(P_ConnectsToMRAD, this.connectsToMRAD, newValue);
        boolean old = connectsToMRAD;
        this.connectsToMRAD = newValue;
        firePropertyChange(P_ConnectsToMRAD, old, this.connectsToMRAD);
    }
    
      

    public void reset() {
        setCode(null);
        setCodeUseNull(false);
        setCodeUseNotNull(false);
        setDnsName(null);
        setUsesCron(false);
        setNull(P_UsesCron);
        setUsesPool(false);
        setNull(P_UsesPool);
        setUsesDns(false);
        setNull(P_UsesDns);
        setDNSShortName(null);
        setClientPort(0);
        setNull(P_ClientPort);
        setWebPort(0);
        setNull(P_WebPort);
        setVipClientPort(0);
        setNull(P_VipClientPort);
        setVipClientPort2(0);
        setNull(P_VipClientPort2);
        setVipWebPort(0);
        setNull(P_VipWebPort);
        setVipWebPort2(0);
        setNull(P_VipWebPort2);
        setUsesIDL(false);
        setNull(P_UsesIDL);
        setConnectsToMRAD(false);
        setNull(P_ConnectsToMRAD);
    }

    public boolean isDataEntered() {
        if (getCode() != null) return true;
        if (getCodeUseNull()) return true;if (getCodeUseNotNull()) return true;
        if (getCodeUseNull()) return true;
        if (getCodeUseNotNull()) return true;
        if (getDnsName() != null) return true;
        if (!isNull(P_UsesCron)) return true;
        if (!isNull(P_UsesPool)) return true;
        if (!isNull(P_UsesDns)) return true;
        if (getDNSShortName() != null) return true;
        if (!isNull(P_ClientPort)) return true;
        if (!isNull(P_WebPort)) return true;


        if (!isNull(P_UsesIDL)) return true;
        if (!isNull(P_ConnectsToMRAD)) return true;
        return false;
    }

    protected String extraWhere;
    protected Object[] extraWhereParams;
    protected OAFilter<ApplicationType> filterExtraWhere;

    public void setExtraWhere(String s, Object ... args) {
        this.extraWhere = s;
        this.extraWhereParams = args;
        if (!OAString.isEmpty(s) && getExtraWhereFilter() == null) {
            OAFilter<ApplicationType> f = new OAQueryFilter<ApplicationType>(ApplicationType.class, s, args);
            setExtraWhereFilter(f);
        }
    }
    public void setExtraWhereFilter(OAFilter<ApplicationType> filter) {
        this.filterExtraWhere = filter;
    }
    public OAFilter<ApplicationType> getExtraWhereFilter() {
        return this.filterExtraWhere;
    }

    public OASelect<ApplicationType> getSelect() {
        String sql = "";
        String sortOrder = "";
        Object[] args = new Object[0];
        if (codeUseNull) {
            if (sql.length() > 0) sql += " AND ";
            sql += ApplicationType.P_Code + " = null";
        }
        else if (codeUseNotNull) {
            if (sql.length() > 0) sql += " AND ";
            sql += ApplicationType.P_Code + " != null";
        }
        else if (!OAString.isEmpty(this.code)) {
            if (sql.length() > 0) sql += " AND ";
            String value = code.replace("*", "%");
            if (!value.endsWith("%")) value += "%";
            if (value.indexOf("%") >= 0) {
                value = value.toUpperCase();
                sql += ApplicationType.P_Code + " LIKE ?";
            }
            else {
                sql += ApplicationType.P_Code + " = ?";
            }
            args = OAArray.add(Object.class, args, value);
        }
        if (!OAString.isEmpty(this.dnsName)) {
            if (sql.length() > 0) sql += " AND ";
            String value = dnsName.replace("*", "%");
            if (!value.endsWith("%")) value += "%";
            if (value.indexOf("%") >= 0) {
                value = value.toUpperCase();
                sql += ApplicationType.P_DnsName + " LIKE ?";
            }
            else {
                sql += ApplicationType.P_DnsName + " = ?";
            }
            args = OAArray.add(Object.class, args, value);
        }
        if (!isNull(P_UsesCron)) {
            if (sql.length() > 0) sql += " AND ";
            sql += ApplicationType.P_UsesCron + " = ?";
            args = OAArray.add(Object.class, args, this.usesCron);
        }
        if (!isNull(P_UsesPool)) {
            if (sql.length() > 0) sql += " AND ";
            sql += ApplicationType.P_UsesPool + " = ?";
            args = OAArray.add(Object.class, args, this.usesPool);
        }
        if (!isNull(P_UsesDns)) {
            if (sql.length() > 0) sql += " AND ";
            sql += ApplicationType.P_UsesDns + " = ?";
            args = OAArray.add(Object.class, args, this.usesDns);
        }
        if (!OAString.isEmpty(this.dnsShortName)) {
            if (sql.length() > 0) sql += " AND ";
            String value = dnsShortName.replace("*", "%");
            if (!value.endsWith("%")) value += "%";
            if (value.indexOf("%") >= 0) {
                value = value.toUpperCase();
                sql += ApplicationType.P_DnsShortName + " LIKE ?";
            }
            else {
                sql += ApplicationType.P_DnsShortName + " = ?";
            }
            args = OAArray.add(Object.class, args, value);
        }
        if (!isNull(P_ClientPort)) {
            if (sql.length() > 0) sql += " AND ";
            sql += ApplicationType.P_ClientPort + " = ?";
            args = OAArray.add(Object.class, args, this.clientPort);
        }
        if (!isNull(P_WebPort)) {
            if (sql.length() > 0) sql += " AND ";
            sql += ApplicationType.P_WebPort + " = ?";
            args = OAArray.add(Object.class, args, this.webPort);
        }
        if (!isNull(P_VipClientPort)) {
            if (sql.length() > 0) sql += " AND ";
            if (!isNull(P_VipClientPort2) && vipClientPort != vipClientPort2) {
                sql += ApplicationType.P_VIPClientPort + " >= ?";
                args = OAArray.add(Object.class, args, getVipClientPort());
                sql += " AND " + ApplicationType.P_VIPClientPort + " <= ?";
                args = OAArray.add(Object.class, args, getVipClientPort2());
            }
            else {
                sql += ApplicationType.P_VIPClientPort + " = ?";
                args = OAArray.add(Object.class, args, getVipClientPort());
            }
        }
        if (!isNull(P_VipWebPort)) {
            if (sql.length() > 0) sql += " AND ";
            if (!isNull(P_VipWebPort2) && vipWebPort != vipWebPort2) {
                sql += ApplicationType.P_VIPWebPort + " >= ?";
                args = OAArray.add(Object.class, args, getVipWebPort());
                sql += " AND " + ApplicationType.P_VIPWebPort + " <= ?";
                args = OAArray.add(Object.class, args, getVipWebPort2());
            }
            else {
                sql += ApplicationType.P_VIPWebPort + " = ?";
                args = OAArray.add(Object.class, args, getVipWebPort());
            }
        }
        if (!isNull(P_UsesIDL)) {
            if (sql.length() > 0) sql += " AND ";
            sql += ApplicationType.P_UsesIDL + " = ?";
            args = OAArray.add(Object.class, args, this.usesIDL);
        }
        if (!isNull(P_ConnectsToMRAD)) {
            if (sql.length() > 0) sql += " AND ";
            sql += ApplicationType.P_ConnectsToMRAD + " = ?";
            args = OAArray.add(Object.class, args, this.connectsToMRAD);
        }

        if (!OAString.isEmpty(extraWhere)) {
            if (sql.length() > 0) sql = "(" + sql + ") AND ";
            sql += extraWhere;
            args = OAArray.add(Object.class, args, extraWhereParams);
        }

        OASelect<ApplicationType> sel = new OASelect<ApplicationType>(ApplicationType.class, sql, args, sortOrder);
        sel.setDataSourceFilter(this.getDataSourceFilter());
        sel.setFilter(this.getCustomFilter());
        return sel;
    }

    private OAFilter<ApplicationType> filterDataSourceFilter;
    public OAFilter<ApplicationType> getDataSourceFilter() {
        if (filterDataSourceFilter != null) return filterDataSourceFilter;
        filterDataSourceFilter = new OAFilter<ApplicationType>() {
            @Override
            public boolean isUsed(ApplicationType applicationType) {
                return ApplicationTypeSearch.this.isUsedForDataSourceFilter(applicationType);
            }
        };
        return filterDataSourceFilter;
    }
    
    private OAFilter<ApplicationType> filterCustomFilter;
    public OAFilter<ApplicationType> getCustomFilter() {
        if (filterCustomFilter != null) return filterCustomFilter;
        filterCustomFilter = new OAFilter<ApplicationType>() {
            @Override
            public boolean isUsed(ApplicationType applicationType) {
                boolean b = ApplicationTypeSearch.this.isUsedForCustomFilter(applicationType);
                if (b && filterExtraWhere != null) b = filterExtraWhere.isUsed(applicationType);
                return b;
            }
        };
        return filterCustomFilter;
    }
    
    public boolean isUsedForDataSourceFilter(ApplicationType applicationType) {
        if (codeUseNull) {
            if (!OACompare.isEmpty(applicationType.getCode())) return false;
        }
        else if (codeUseNotNull) {
            if (OACompare.isEmpty(applicationType.getCode())) return false;
        }
        else if (code != null) {
            String s = getCode();
            if (s != null && s.indexOf('*') < 0 && s.indexOf('%') < 0) s += '*';
            if (!OACompare.isLike(applicationType.getCode(), s)) return false;
        }
        if (dnsName != null) {
            String s = getDnsName();
            if (s != null && s.indexOf('*') < 0 && s.indexOf('%') < 0) s += '*';
            if (!OACompare.isLike(applicationType.getDnsName(), s)) return false;
        }
        if (!isNull(P_UsesCron)) {
            if (!OACompare.isEqual(applicationType.getUsesCron(), usesCron)) return false;
        }
        if (!isNull(P_UsesPool)) {
            if (!OACompare.isEqual(applicationType.getUsesPool(), usesPool)) return false;
        }
        if (!isNull(P_UsesDns)) {
            if (!OACompare.isEqual(applicationType.getUsesDns(), usesDns)) return false;
        }
        if (dnsShortName != null) {
            String s = getDNSShortName();
            if (s != null && s.indexOf('*') < 0 && s.indexOf('%') < 0) s += '*';
            if (!OACompare.isLike(applicationType.getDnsShortName(), s)) return false;
        }
        if (!isNull(P_ClientPort)) {
            if (!OACompare.isEqual(applicationType.getClientPort(), clientPort)) return false;
        }
        if (!isNull(P_WebPort)) {
            if (!OACompare.isEqual(applicationType.getWebPort(), webPort)) return false;
        }
        if (!isNull(P_VipClientPort2)) {
            if (!OACompare.isEqualOrBetween(applicationType.getVIPClientPort(), vipClientPort, vipClientPort2)) return false;
        }
        if (!isNull(P_VipWebPort2)) {
            if (!OACompare.isEqualOrBetween(applicationType.getVIPWebPort(), vipWebPort, vipWebPort2)) return false;
        }
        if (!isNull(P_UsesIDL)) {
            if (!OACompare.isEqual(applicationType.getUsesIDL(), usesIDL)) return false;
        }
        if (!isNull(P_ConnectsToMRAD)) {
            if (!OACompare.isEqual(applicationType.getConnectsToMRAD(), connectsToMRAD)) return false;
        }
        return true;
    }
    public boolean isUsedForCustomFilter(ApplicationType applicationType) {
        return true;
    }
}
