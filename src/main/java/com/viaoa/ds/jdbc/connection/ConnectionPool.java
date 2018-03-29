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
package com.viaoa.ds.jdbc.connection;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
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
    
    private DBMetaData dbmd;
    private ArrayList<OAConnection> alOAConnection = new ArrayList<OAConnection>();
    private transient Thread thread; // used to release connections
    private boolean bStopThread;     // tells thread to stop
    private Object threadLOCK = new Object();
    
    private final ReentrantLock lock = new ReentrantLock();
    
    /**
        Create new Pool that is used for a OADataSourceJDBC.
    */
    public ConnectionPool(DBMetaData dbmd) {
        this.dbmd = dbmd;

        // start a monitor thread that will release connections when not used
        open();
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
    
    /** 
        Low priority Thread used to close extra connections that are not being used. 
        Runs every 10 minutes.
    */
    public void run() {
        if (dbmd.minConnections < 1) {
            LOG.warning("dbmd.minConnections="+dbmd.minConnections+", will use one instead");
            dbmd.minConnections = 1;
        }
        if (dbmd.maxConnections < dbmd.minConnections) {
            LOG.warning("invalid dbmd.maxConnections="+dbmd.maxConnections+" is less then dbmd.minConnections="+dbmd.minConnections+", will use "+dbmd.minConnections+"+1 for max");
            dbmd.maxConnections = dbmd.minConnections + 1;
        }
        for ( ; !bStopThread; ) {
            int cntAvailable = 0;
            int cntClosed = 0;
            
            try {
                lock.lock();
                for (int i=0; i<alOAConnection.size(); i++) {
                    OAConnection con = alOAConnection.get(i);
                    if (con.connection.isClosed()) {
                        alOAConnection.remove(i);
                        i--;
                        continue;
                    }
                    if (!con.bAvailable) continue;
                    if (con.getTotalUsed() > 0) continue;
                    if (++cntAvailable <= dbmd.minConnections) continue;  // keep min connections
                    
                    con.connection.close();
                    alOAConnection.remove(i);
                    i--;
                    
                    if (++cntClosed == 2) break; // only release max 2 at each check.
                }
            }
            catch (java.sql.SQLException e) {
                LOG.log(Level.WARNING, "exception while checking connections, will continue", e);
            }
            finally {
                lock.unlock();
            }

            for (int i=cntAvailable; i<dbmd.minConnections && (alOAConnection.size() < dbmd.maxConnections) ; i++) {
                boolean bLocked = false;
                try {
                    OAConnection con = createNewOAConnection();
                    bLocked = true;
                    lock.lock();
                    if (alOAConnection.size() >= dbmd.maxConnections) break;
                    con.bAvailable = true;
                    alOAConnection.add(con);
                }
                catch (Exception e) {
                    LOG.log(Level.WARNING, "error trying to create a new JDBC connection", e);
                }
                finally {
                    if (bLocked) lock.unlock();
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
        Returns true if database is still connected.
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
        try {
            lock.lock();
            for (OAConnection con : alOAConnection) {
                try {
                    if (!con.connection.isClosed()) {
                        con.connection.close();
                    }
                }
                catch (Exception e) {
                    System.out.println("Connection.close() exception: "+e);
                    e.printStackTrace();
                }
            }
            alOAConnection.clear();
        }
        finally {
            lock.unlock();
        }
    }
    
    
    /**
     * Returns an unused JDBC connection, null if maxConnections has been reached and all current connections are used.
     */
    public Connection getConnection(boolean bExclusive) throws Exception {
        OAConnection c = getOAConnection(false, bExclusive);
        if (c == null) return null;
        return c.connection;
    }

    
    protected OAConnection getOAConnection(boolean bForStatement, boolean bExclusive) throws Exception {
        OATransaction tran = OAThreadLocalDelegate.getTransaction();

        OAConnection con = null;
        if (tran != null) {
            con = (OAConnection) tran.get(this);
            if (con != null) return con;
            bExclusive = true;
        }
        if (!bExclusive && !dbmd.getAllowStatementPooling()) bExclusive = true;
        
        try {
            lock.lock();
            
            for (OAConnection conx : alOAConnection) {
                if (!conx.bAvailable) continue;
                int used = conx.getTotalUsed(); 
                if (bExclusive) {
                    if (used > 0) continue;
                }
                if (con == null || used <= con.getTotalUsed()) {
                    con = conx;
                    if (used == 0) break;
                }
            }

            boolean bMaxed = (alOAConnection.size() >= dbmd.maxConnections);
            if (con != null) {
                int used = con.getTotalUsed(); 
                if (used > 0 && !bMaxed) {
                    con = null;
                }
                else {
                    con.bAvailable = !bExclusive;
                    if (bForStatement) con.bGettingStatement = true;
                }
            }
            else if (bMaxed) {
                return null;
            }
        }
        finally {
            lock.unlock();
        }

        
        if (con == null) {
            con = createNewOAConnection();
            try {
                lock.lock();
                con.bAvailable = !bExclusive;
                if (bForStatement) con.bGettingStatement = true;
                alOAConnection.add(con);
            }
            finally {
                lock.unlock();
            }
        }

        if (tran != null) {
            con.connection.setTransactionIsolation(tran.getTransactionIsolationLevel());
            con.connection.setAutoCommit(false);
            tran.put(this, con);
            MyOATransactionListener tl = new MyOATransactionListener(con);
            tran.addTransactionListener(tl);
        }
        return con;
    }

    protected OAConnection createNewOAConnection() throws Exception {
        Class.forName(dbmd.driverJDBC).newInstance();
        Connection connection = DriverManager.getConnection(dbmd.urlJDBC, dbmd.user, dbmd.password);
        connection.setAutoCommit(true);
        connection.setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);
        OAConnection oacon = new OAConnection(connection);
        return oacon;
    }
    
    public void releaseConnection(Connection connection) {
        try {
            lock.lock();
            for (OAConnection con : alOAConnection) {
                if (con.connection != connection) continue;
                try {
                    connection.setAutoCommit(true);
                    connection.setTransactionIsolation(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED);
                    con.bAvailable = true;
                }
                catch (SQLException e) {
                    LOG.log(Level.WARNING, "releaseConnection() exception", e);
                }
                break;
            }
        }
        finally {
            lock.unlock();
        }
    }
    
    protected OAConnection getStatementConnection() throws Exception {
        for (int i=0; i<100; i++) {
            OAConnection c = getOAConnection(true, false);
            if (c != null) {
                return c;
            }
            Thread.sleep(2);
        }
        return null;
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
    
    /**
        Returns a JDBC Statement that can be used for direct JDBC calls.
        @param message reason/description for using statement.  This is used by getInfo(),
    */  
    public Statement getStatement(String message) throws Exception {
        OAConnection con = getStatementConnection();
        Statement statement;
        try {
            statement = con.getStatement(message);
        }
        catch (Exception e) {
            if (con != null && con.connection.isClosed()) return getStatement(message);
            throw e;
        }
        statement.setMaxRows(0);
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
        Object[] objs = null;
        try {
            lock.lock();
            objs = alOAConnection.toArray();
        }
        finally {
            lock.unlock();
        }
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
        ps.setMaxRows(0);
        return ps;
    }

    /**
        Release a PreparedStatement obtained from getPreparedStatement.
    */
    public void releasePreparedStatement(PreparedStatement statement, boolean bCanBeReused) {
        if (statement == null) return;
        Object[] objs = null;
        try {
            lock.lock();
            objs = alOAConnection.toArray();
        }
        finally {
            lock.unlock();
        }
        for (Object objx : objs) {
            OAConnection con = (OAConnection) objx;
            if (con.releasePreparedStatement(statement, bCanBeReused)) {
                break;
            }
        }
    }

    /**
	    Called by OADataSource.getInfo to return information about database connections.
	*/
	public void getInfo(Vector<Object> vec) {
	    vec.addElement("Driver: "+dbmd.driverJDBC);
	    vec.addElement("URL: "+dbmd.urlJDBC);
	    vec.addElement("User: "+dbmd.user);
	    vec.addElement("Min Connections: "+dbmd.minConnections);
	    vec.addElement("Max Connections: "+dbmd.maxConnections);
	    vec.addElement("Connections");

	    try {
	        lock.lock();
	        int cnter = 0;
            for (OAConnection con : alOAConnection) {
                String s = String.format("%d) JDBC Connection, Statements current=%d/used=%d/created=%,d/queries=%,d," +
                		" Prepared current=%d/used=%d/created=%,d/queries=%,d", 
                        cnter++, 
                        con.vecStatement.size(), con.getCurrentlyUsedStatementCount(), con.cntCreateStatement, con.cntGetStatement,
                        con.getTotalPreparedStatements(), con.vecUsedPreparedStatement.size(), con.cntCreatePreparedStatement, con.cntGetPreparedStatement 
                );
                if (!con.bAvailable) s += (" * connection not available");

                vec.addElement(s);
                con.getInfo(vec);               
            }
	    }
	    finally {
	        lock.unlock();
	    }
	}
}

