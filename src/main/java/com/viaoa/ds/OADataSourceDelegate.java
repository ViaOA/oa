/*  Copyright 1999-2015 Vince Via vvia@viaoa.com
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
*/
package com.viaoa.ds;

import java.sql.Connection;
import java.sql.Statement;

import com.viaoa.ds.jdbc.OADataSourceJDBC;
import com.viaoa.ds.jdbc.connection.OAConnection;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.transaction.OATransaction;

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
