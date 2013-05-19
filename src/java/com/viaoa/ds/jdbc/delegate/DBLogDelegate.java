package com.viaoa.ds.jdbc.delegate;

import java.util.logging.Level;
import java.util.logging.Logger;


/**
   This can be used to log all DB activity using a Logger.
   
   <p>
    Example: <pre>
    String s = "com.viaoa.ds.jdbc.delegate.DBLogDelegate";        
    log = Logger.getLogger(s);
    log.setLevel(Level.FINE);
    fh = createFileHandler("DBChanges", Level.FINE);
    log.addHandler(fh);
    </pre>
 */
public class DBLogDelegate {
    private static Logger LOG = Logger.getLogger(DBLogDelegate.class.getName());
    
    public static void logDelete(String sql) {
        LOG.fine("DELETE: [[BEGIN[" + sql + "]END]]");
    }

    public static void logDDL(String sql) {
        LOG.fine("DDL: [[BEGIN[" + sql + "]END]]");
    }
    
    public static void logInsert(String sql, Object[] params) {
        String s = "";
        for (int i=0; params != null && i < params.length; i++) {
            s += "[[PARAM"+i+"[" + params[i] + "]END]]"; 
        }
        LOG.fine("INSERT: [[BEGIN[" + sql + s + "]END]]");
    }
    
    public static void logUpdate(String sql, Object[] params) {
        String s = "";
        for (int i=0; params != null && i < params.length; i++) {
            s += "[[PARAM"+i+"[" + params[i] + "]END]]"; 
        }
        LOG.fine("UPDATE: [[BEGIN[" + sql + s + "]END]]");
    }

    
}
