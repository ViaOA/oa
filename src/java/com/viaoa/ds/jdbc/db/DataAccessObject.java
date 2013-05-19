package com.viaoa.ds.jdbc.db;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.viaoa.object.OAObject;

/**
 * Used for populated OAObject properties with resultSet data.
 * @author vvia
 *
 */
public interface DataAccessObject {

    public class ResultSetInfo {
        ResultSet rs;
        boolean foundInCache;
        public void reset(ResultSet rs) {
            this.rs = rs;
            foundInCache = false;
        }
        public boolean getFoundInCache() {
            return this.foundInCache;
        }
        public void setFoundInCache(boolean b) {
            this.foundInCache = b;
        }
        public ResultSet getResultSet() {
            return rs;
        }
    }
    
    public OAObject getObject(ResultSetInfo rsi) throws SQLException;
    public String getPkeySelectColumns();
    public String getSelectColumns();
    
}
