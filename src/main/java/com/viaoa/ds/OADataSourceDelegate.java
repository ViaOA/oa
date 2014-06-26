package com.viaoa.ds;

import java.sql.Connection;
import java.sql.Statement;

import com.viaoa.ds.jdbc.OADataSourceJDBC;

public class OADataSourceDelegate {

    public static OADataSourceJDBC getJDBCDataSource() throws Exception {
        OADataSource[] dss = OADataSource.getDataSources();
        if (dss == null) return null;
        for (OADataSource ds : dss) {
            if (ds instanceof OADataSourceJDBC) {
                OADataSourceJDBC jds = (OADataSourceJDBC) ds;
                return jds;
            }
        }
        return null;
    }

    
    public static Connection getConnection() throws Exception {
        OADataSource[] dss = OADataSource.getDataSources();
        if (dss == null) return null;
        for (OADataSource ds : dss) {
            if (ds instanceof OADataSourceJDBC) {
                OADataSourceJDBC jds = (OADataSourceJDBC) ds;
                return jds.getConnection();
            }
        }
        return null;
    }
    public static void releaseConnection(Connection connection) {
        if (connection == null) return;
        OADataSource[] dss = OADataSource.getDataSources();
        if (dss == null) return;
        for (OADataSource ds : dss) {
            if (ds instanceof OADataSourceJDBC) {
                OADataSourceJDBC jds = (OADataSourceJDBC) ds;
                jds.releaseConnection(connection);
                break;
            }
        }
    }
    
    
    public static Statement getStatement() throws Exception {
        OADataSource[] dss = OADataSource.getDataSources();
        if (dss == null) return null;
        for (OADataSource ds : dss) {
            if (ds instanceof OADataSourceJDBC) {
                OADataSourceJDBC jds = (OADataSourceJDBC) ds;
                return jds.getStatement("");
            }
        }
        return null;
    }

    public static Statement getStatement(String msg) throws Exception {
        OADataSource[] dss = OADataSource.getDataSources();
        if (dss == null) return null;
        for (OADataSource ds : dss) {
            if (ds instanceof OADataSourceJDBC) {
                OADataSourceJDBC jds = (OADataSourceJDBC) ds;
                return jds.getStatement(msg);
            }
        }
        return null;
    }

    public static void releaseStatement(Statement statement) {
        if (statement == null) return;
        OADataSource[] dss = OADataSource.getDataSources();
        if (dss == null) return;
        for (OADataSource ds : dss) {
            if (ds instanceof OADataSourceJDBC) {
                OADataSourceJDBC jds = (OADataSourceJDBC) ds;
                jds.releaseStatement(statement);
                break;
            }
        }
    }
}
