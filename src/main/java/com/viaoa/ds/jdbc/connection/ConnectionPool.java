/*
This software and documentation is the confidential and proprietary
information of ViaOA, Inc. ("Confidential Information").
You shall not disclose such Confidential Information and shall use
it only in accordance with the terms of the license agreement you
entered into with ViaOA, Inc.

ViaOA, Inc. MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE, OR NON-INFRINGEMENT. ViaOA, Inc. SHALL NOT BE LIABLE FOR ANY DAMAGES
SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
THIS SOFTWARE OR ITS DERIVATIVES.

Copyright (c) 2001-2013 ViaOA, Inc.
All rights reserved.
*/
package com.viaoa.ds.jdbc.connection;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.*;

import com.viaoa.ds.jdbc.db.*;
import com.viaoa.object.OAThreadLocalDelegate;
import com.viaoa.transaction.OATransaction;
import com.viaoa.transaction.OATransactionListener;

/** 
    Maintains a dynamic pool of connections to database.  These connections
    are then internally managed by OADataSource.
*/
public class ConnectionPool implements Runnable {
    private static Logger LOG = Logger.getLogger(ConnectionPool.class.getName());
    
    private Vector vecConnection = new Vector(10,10);
    private transient Thread thread; // used to release connections
    private DBMetaData dbmd;
    private boolean bStopThread;     // tells thread to stop
    private Object threadLOCK = new Object();
    
    /**
        Create new Pool that is used for a OADataSourceJDBC.
    */
    public ConnectionPool(DBMetaData dbmd) {
        this.dbmd = dbmd;

        // start a monitor thread that will release connections when not used
        open();
    }

    public void close() {
    	if (thread != null) {
	        thread = null;
	        bStopThread = true;
	        synchronized (threadLOCK) {
	            threadLOCK.notify();
	        }
	        closeAllConnections();	        
    	}
    }

    public void open() {
        bStopThread = false;
        if (thread == null) {
            thread = new Thread(this, "OAConnectionPool"); // used to release connections
            thread.setDaemon(true);
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.start();
        }
    }
    
    /** 
        Low priority Thread used to close extra connections that are not being used. 
        Runs every 10 minutes.
    */
    public void run() {
        for ( ; !bStopThread; ) {
            synchronized(vecConnection) {
                int x = vecConnection.size();
                int cntClosed = 0;
                int current = 0;
                for (int i=0; i<x; i++) {
                    OAConnection con = (OAConnection) vecConnection.elementAt(i);
                    try {
                        if (con.connection.isClosed()) continue;
                        current++;
                        if (!con.bAvailable) continue;
                        if (con.getNumberOfUsedStatements() > 0) continue;
                        if (current <= dbmd.minConnections) continue;
                        
                        con.connection.close();
                        if (++cntClosed == 2) break; // only release max 2 at each check.
                    }
                    catch (java.sql.SQLException e) {
                        System.out.println("Connection.run() exception: "+e);
                        e.printStackTrace();
                    }
                }
            }
            try {
                synchronized (threadLOCK) {
                	if (!bStopThread) threadLOCK.wait(1000 * 60 * 5);
                }
            }
            catch (InterruptedException e) {
            }
        }
    }

    /**
        Returns true is database is still connected.
    */
    public boolean isDatabaseAvailable() {
        try {
            Statement st = getStatement("OADataSourceJDBC.ConnectionPool.isDatabaseAvailable()");
            releaseStatement(st);
        }
        catch (Exception e) {
            LOG.log(Level.WARNING, "error checking database", e);
            return false;
        }
        return true;
    }

    /**
        Close all connections and remove from Connection Pool.
    */
    public void closeAllConnections() {
        synchronized(vecConnection) {
            int x = vecConnection.size();
            for (int i=0; i<x; i++) {
                OAConnection con = (OAConnection) vecConnection.elementAt(i);
                try {
                    if (!con.connection.isClosed()) {
                        con.connection.close();
                    }
                }
                catch (SQLException e) {
                    System.out.println("Connection.close() exception: "+e);
                    e.printStackTrace();
                }
            }
            vecConnection.clear();
        }
    }
    
    
    /**
     * Returns an unused JDBC connection, null if maxConnections has been reached and all current connections are used.
     */
    public Connection getConnection() throws Exception {
        OAConnection c = getOAConnection();
        if (c == null) return null;
        return c.connection;
    }
    
    protected OAConnection getOAConnection() throws Exception {
        OATransaction tran = OAThreadLocalDelegate.getTransaction();

        OAConnection con = null;
        if (tran != null) {
            con = (OAConnection) tran.get(this);
            if (con != null) return con;
        }

        synchronized(vecConnection) {
            int x = vecConnection.size();
            for (int i=(x-1); i>=0; i--) {
                con = (OAConnection) vecConnection.elementAt(i);
                if (!con.bAvailable) continue;
                if (con.connection.isClosed()) {
                    vecConnection.removeElementAt(i);
                    x--;
                    continue;
                }
                if ((tran != null || !dbmd.getAllowStatementPooling()) && con.getNumberOfUsedStatements() > 0) continue;
                break;
            }
            if (con != null) {
                con.bAvailable = (tran == null);
            }
            else if (x >= dbmd.maxConnections) {
                return null;
            }
            
        }

        if (con == null) {
            Class.forName(dbmd.driverJDBC).newInstance();
            Connection connection = DriverManager.getConnection(dbmd.urlJDBC, dbmd.user, dbmd.password);
            connection.setAutoCommit(true);
            connection.setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);
            con = new OAConnection(connection);
        }
        
        synchronized(vecConnection) {
            int x = vecConnection.size();
            if (x < dbmd.maxConnections) {
                vecConnection.addElement(con);
            }
            else {
                con = null;
            }
        }

        if (tran != null && con != null) {
            con.connection.setTransactionIsolation(tran.getTransactionIsolationLevel());
            con.connection.setAutoCommit(false);
            tran.put(this, con);
            MyOATransactionListener tl = new MyOATransactionListener(con);
            tran.addTransactionListener(tl);
        }

        return con;
    }

    public void releaseConnection(Connection connection) {
        Object[] objs = vecConnection.toArray();
        for (Object objx : objs) {
            OAConnection con = (OAConnection) objx;
            if (con.connection == connection) {
                try {
                    connection.setAutoCommit(true);
                    connection.setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);
                    con.bAvailable = true;
                }
                catch (SQLException e) {
                    System.out.println("releaseConnection() exception: "+e);
                    e.printStackTrace();
                }
                break;
            }
        }
    }
    
    protected OAConnection getStatementConnection() throws Exception {
        for (int i=0; i<100; i++) {
            OAConnection c = _getStatementConnection();
            if (c != null) return c;
            Thread.sleep(25);
        }
        return null;
    }
        

    /**
     * Returns an unused JDBC connection, null if maxConnections has been reached and all current connections are used.
     */
    private OAConnection _getStatementConnection() throws Exception {
        OATransaction tran = OAThreadLocalDelegate.getTransaction();
        if (tran != null) {
            OAConnection con = (OAConnection) tran.get(this);
            if (con != null) return con;
        }

        OAConnection conx = null;
        if (tran != null) {
            conx = getOAConnection();
        }
        else {
            int x;
            // use an existing connection
            synchronized(vecConnection) {
                x = vecConnection.size();
                for (int i=(x-1); i>=0; i--) {
                    OAConnection c = (OAConnection) vecConnection.elementAt(i);
                    if (c.connection.isClosed()) {
                        vecConnection.removeElementAt(i);
                        x--;
                        continue;
                    }
                    if (!c.bAvailable) continue;
                    if (!dbmd.getAllowStatementPooling() && c.getNumberOfUsedStatements() > 0) continue;
                    if (conx == null || c.getNumberOfUsedStatements() < conx.getNumberOfUsedStatements()) conx = c;
                }
            }
            if (conx == null || (conx.getNumberOfUsedStatements() > 0 && x < dbmd.maxConnections)) {
                conx = getOAConnection();
            }
        }
        return conx;
    }
    

    class MyOATransactionListener implements OATransactionListener {
        OAConnection conx;
        public MyOATransactionListener(OAConnection con) throws Exception {
            this.conx = con;
        }
        @Override
        public void commit(OATransaction t) {
            if (conx != null) {
                try {
                    conx.connection.commit();
                }
                catch (SQLException e) {
                    LOG.log(Level.WARNING, "OATransactionListener.commit()", e);
                }
                finally {
                    releaseConnection(conx.connection);
                }
            }
        }
        @Override
        public void rollback(OATransaction t) {
            if (conx != null) {
                try {
                    conx.connection.rollback();
                }
                catch (SQLException e) {
                    LOG.log(Level.WARNING, "OATransactionListener.rollback()", e);
                }
                finally {
                    releaseConnection(conx.connection);
                }
            }
        }
    }
    
// temp fix    
    // 20120625 temp added synchronized, since there is a problem with resultSets getting closed on Hi5 derby database
//qqqqqqq more then one thread cant share a connection if there are LOB involved.  Derby uses ThreadLocal to manage them    
    /**
        Returns a JDBC Statement that can be used for direct JDBC calls.
        @message message reason/description for using statement.  This is used by getInfo(),
    */  
    public synchronized Statement getStatement(String message) throws Exception {
        if (dbmd.minConnections < 1) {
            LOG.warning("dbmd.minConnections="+dbmd.minConnections+", will use one instead");
            dbmd.minConnections = 1;
        }
        if (dbmd.maxConnections < dbmd.minConnections) {
            LOG.warning("invalid dbmd.maxConnections="+dbmd.maxConnections+" is less then dbmd.minConnections="+dbmd.minConnections+", will use "+dbmd.minConnections+" for max");
            dbmd.maxConnections = dbmd.minConnections;
        }
        OAConnection con = getStatementConnection();
        
        Statement statement;
        try {
            statement = con.getStatement(message);
        }
        catch (Exception e) {
            if (con.connection.isClosed()) return getStatement(message);
            throw e;
        }
        if (dbmd.databaseType != dbmd.ACCESS) {
            statement.setQueryTimeout(0);  // Access wont allow using JDBC Bridge
        }
        
        return statement;
    }

    /**
        Release a Statment obtained from getStatement.
    */
    public void releaseStatement(Statement statement) {
        if (statement == null) return;
        Object[] objs = vecConnection.toArray();
        for (Object objx : objs) {
            OAConnection con = (OAConnection) objx;
            if (con.releaseStatement(statement)) {
                break;
            }
        }
    }


    /**
        Returns a JDBC PreparedStatment that can be used for direct JDBC calls.
        @param sql to assign to prepared statement.
        @param bHasAutoGenerated true if this is an insert that will have a generated pkey
    */  
    public PreparedStatement getPreparedStatement(String sql, boolean bHasAutoGenerated) throws Exception {
        if (dbmd.minConnections < 1) throw new Exception("OADataSourceJDBC.ConnectionPool.minimumConnections is less then one, call OADataSourceJDBC.setMinConnections(x) to set");
        if (dbmd.maxConnections < dbmd.minConnections) throw new Exception("OADataSourceJDBC.ConnectionPool.maximumConnections is less then minimumConnections, call OADataSourceJDBC.setMaxConnections(x) to set");

        OAConnection con = getStatementConnection();

        PreparedStatement ps;
        try {
        	if (dbmd.getSupportsAutoAssign()) ps = con.getPreparedStatement(sql, bHasAutoGenerated);
        	else ps = con.getPreparedStatement(sql, false);
        }
        catch (Exception e) {
            if (con.connection.isClosed()) {
            	return getPreparedStatement(sql, bHasAutoGenerated);
            }
            throw e;
        }
        if (dbmd.databaseType != dbmd.ACCESS) {
            ps.setQueryTimeout(0);  // Access wont allow using JDBC Bridge
        }
        return ps;
    }

    /**
        Release a PreparedStatement obtained from getPreparedStatement.
    */
    public void releasePreparedStatement(PreparedStatement ps, boolean bCanBeReused) {
        if (ps == null) return;
        Object[] objs = vecConnection.toArray();
        for (Object objx : objs) {
            OAConnection con = (OAConnection) objx;
            if (con.releasePreparedStatement(ps, bCanBeReused)) {
            	break;
            }
        }
    }


    /**
        Creates new connection and adds it to connection pool.
    */
    protected OAConnection createConnection() throws Exception {
        Class.forName(dbmd.driverJDBC).newInstance();
        OAConnection myCon = null;

        boolean b = true;  // create at least one more connection
        for (int i=0; b || i < dbmd.minConnections; i++) {
            b = false;
            Connection connection = DriverManager.getConnection(dbmd.urlJDBC, dbmd.user, dbmd.password);
            connection.setAutoCommit(true); 
            myCon = new OAConnection(connection);
            myCon.bAvailable = false; // make sure that this cant be used yet.
            synchronized(vecConnection) {
            	vecConnection.addElement(myCon);
            }
        }
        return myCon;
    }

    
    /**
	    Called by OADataSource.getInfo to return information about database connections.
	*/
	public void getInfo(Vector vec) {
	    vec.addElement("Driver: "+dbmd.driverJDBC);
	    vec.addElement("URL: "+dbmd.urlJDBC);
	    vec.addElement("User: "+dbmd.user);
	    vec.addElement("Min Connections: "+dbmd.minConnections);
	    vec.addElement("Max Connections: "+dbmd.maxConnections);
	    vec.addElement("Connections");
	
	    synchronized(vecConnection) {
	        int x = vecConnection.size();
	        for (int i=0; i < x; i++) {
	            OAConnection con = (OAConnection) vecConnection.elementAt(i);
	            // vec.addElement(i+") Total Statements: "+con.vecStatement.size() + "/ Used: "+con.getNumberOfUsedStatements());
                vec.addElement(i+") JDBC Connection, total Statements: "+con.vecStatement.size() + "/ Used: "+con.getNumberOfUsedStatements()+", statementsUsed="+con.getStatementUsedCount());
                if (!con.bAvailable) vec.addElement(" * connection not available");

	            con.getInfo(vec);	            
	        }
	    }
	}

}

